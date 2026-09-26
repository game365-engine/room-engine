package com.roomengine.examples.party;

import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;
import com.roomengine.core.model.Room;
import com.roomengine.core.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public final class PartyGameService {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 8;
    private static final int MAX_ROUNDS = 3;
    private static final int ROLL_MIN = 1;
    private static final int ROLL_MAX = 6;

    private final RoomService roomService;
    private final Map<String, GameState> gameStateMap = new ConcurrentHashMap<>();

    public PartyGameService(RoomService roomService) {
        this.roomService = roomService;
    }

    public synchronized GameState createGame(int capacity) {
        if (capacity < MIN_PLAYERS || capacity > MAX_PLAYERS) {
            throw new RoomException(ErrorCode.INVALID_ARGUMENT);
        }
        Room room = roomService.createRoom(capacity);
        GameState state = newState(room.id(), room.capacity());
        gameStateMap.put(room.id(), state);
        return state;
    }

    public synchronized GameState joinPlayer(String roomId, String playerId, String displayName) {
        GameState current = getGame(roomId);
        ensurePhase(current, PartyPhase.WAITING);
        roomService.joinRoom(roomId, playerId, displayName);
        return save(withPlayers(current, roomService.findRoom(roomId).orElseThrow(
                () -> new RoomException(ErrorCode.ROOM_NOT_FOUND))));
    }

    public synchronized GameState leavePlayer(String roomId, String playerId) {
        GameState current = getGame(roomId);
        ensurePhase(current, PartyPhase.WAITING);
        if (!current.hasPlayer(playerId)) {
            throw new PartyGameException(PartyErrorCode.PLAYER_NOT_IN_ROOM);
        }
        roomService.leaveRoom(roomId, playerId);
        Room room = roomService.findRoom(roomId).orElse(null);
        if (room == null) {
            gameStateMap.remove(roomId);
            throw new PartyGameException(PartyErrorCode.ROOM_NOT_FOUND);
        }
        if (room.players().isEmpty()) {
            roomService.closeRoom(roomId);
            gameStateMap.remove(roomId);
            return withPlayers(current, room);
        }
        GameState updated = withPlayers(current, room);
        gameStateMap.put(roomId, updated);
        return updated;
    }

    public synchronized void closeGame(String roomId) {
        getGame(roomId);
        roomService.closeRoom(roomId);
        gameStateMap.remove(roomId);
    }

    public GameState getGame(String roomId) {
        GameState state = gameStateMap.get(roomId);
        if (state == null) {
            throw new PartyGameException(PartyErrorCode.ROOM_NOT_FOUND);
        }
        return state;
    }

    public synchronized GameState markReady(String roomId, String playerId) {
        GameState current = getGame(roomId);
        ensurePlayer(current, playerId);
        ensurePhase(current, PartyPhase.WAITING);

        Map<String, PlayerState> players = copyPlayers(current.players());
        PlayerState player = players.get(playerId);
        players.put(playerId, player.withReady(true));
        return save(current.withPlayers(players));
    }

    public synchronized GameState startGame(String roomId, String playerId) {
        GameState current = getGame(roomId);
        ensurePlayer(current, playerId);
        ensurePhase(current, PartyPhase.WAITING);
        if (!current.hostPlayerId().equals(playerId)) {
            throw new PartyGameException(PartyErrorCode.NOT_HOST);
        }
        if (current.players().size() < MIN_PLAYERS) {
            throw new PartyGameException(PartyErrorCode.NOT_ENOUGH_PLAYERS);
        }
        if (current.players().values().stream().anyMatch(player -> !player.ready())) {
            throw new PartyGameException(PartyErrorCode.NOT_READY);
        }

        String firstPlayerId = current.players().keySet().iterator().next();
        return save(current.withPhase(PartyPhase.PLAYING)
                .withRoundNumber(1)
                .withTurnNumber(1)
                .withCurrentPlayerId(firstPlayerId)
                .withLastAction(null));
    }

    public synchronized GameState takeTurn(String roomId, String playerId, String actionValue) {
        GameState current = getGame(roomId);
        ensurePlayer(current, playerId);
        ensurePhase(current, PartyPhase.PLAYING);
        if (!current.currentPlayerId().equals(playerId)) {
            throw new PartyGameException(PartyErrorCode.NOT_YOUR_TURN);
        }

        PartyAction action = PartyAction.parse(actionValue);
        int value = action == PartyAction.ROLL
                ? ThreadLocalRandom.current().nextInt(ROLL_MIN, ROLL_MAX + 1)
                : 0;
        Map<String, PlayerState> players = copyPlayers(current.players());
        PlayerState player = players.get(playerId);
        players.put(playerId, player.withScore(player.score() + value));

        int nextTurnNumber = current.turnNumber() + 1;
        int nextRoundNumber = current.roundNumber();
        String nextPlayerId = nextPlayerId(current.players(), playerId);
        PartyPhase nextPhase = PartyPhase.PLAYING;
        if (nextPlayerId.equals(current.players().keySet().iterator().next())) {
            if (current.roundNumber() == MAX_ROUNDS) {
                nextPhase = PartyPhase.FINISHED;
                nextPlayerId = null;
                nextTurnNumber = current.turnNumber();
            } else {
                nextRoundNumber += 1;
            }
        }

        LastAction lastAction = new LastAction(playerId, action.name().toLowerCase(), value);
        return save(current.withPlayers(players)
                .withPhase(nextPhase)
                .withRoundNumber(nextRoundNumber)
                .withTurnNumber(nextTurnNumber)
                .withCurrentPlayerId(nextPlayerId)
                .withLastAction(lastAction)
                .withWinner(nextPhase == PartyPhase.FINISHED ? findWinner(players) : null));
    }

    public void ensurePlayer(String roomId, String playerId) {
        ensurePlayer(getGame(roomId), playerId);
    }

    private void ensurePlayer(GameState state, String playerId) {
        if (playerId == null || !state.hasPlayer(playerId)) {
            throw new PartyGameException(PartyErrorCode.PLAYER_NOT_IN_ROOM);
        }
    }

    private void ensurePhase(GameState state, PartyPhase expectedPhase) {
        if (state.phase() != expectedPhase) {
            if (state.phase() == PartyPhase.FINISHED) {
                throw new PartyGameException(PartyErrorCode.GAME_FINISHED);
            }
            throw new PartyGameException(PartyErrorCode.GAME_ALREADY_STARTED);
        }
    }

    private GameState save(GameState state) {
        gameStateMap.put(state.roomId(), state);
        return state;
    }

    private GameState newState(String roomId, int capacity) {
        return new GameState(roomId, capacity, PartyPhase.WAITING, 0, 0, null, null,
                new LinkedHashMap<>(), null, null);
    }

    private GameState withPlayers(GameState state, Room room) {
        Map<String, PlayerState> players = copyPlayers(state.players());
        room.players().values().forEach(player -> players.putIfAbsent(
                player.id(), new PlayerState(player.id(), player.displayName(), false, 0)));
        players.keySet().removeIf(playerId -> !room.players().containsKey(playerId));
        String hostPlayerId = state.hostPlayerId();
        if (hostPlayerId == null && !players.isEmpty()) {
            hostPlayerId = players.keySet().iterator().next();
        }
        if (hostPlayerId != null && !players.containsKey(hostPlayerId)) {
            hostPlayerId = players.isEmpty() ? null : players.keySet().iterator().next();
        }
        return state.withRoom(room).withPlayers(players).withHostPlayerId(hostPlayerId);
    }

    private Map<String, PlayerState> copyPlayers(Map<String, PlayerState> players) {
        return new LinkedHashMap<>(players);
    }

    private String nextPlayerId(Map<String, PlayerState> players, String currentPlayerId) {
        List<String> playerIds = new ArrayList<>(players.keySet());
        int currentIndex = playerIds.indexOf(currentPlayerId);
        return playerIds.get((currentIndex + 1) % playerIds.size());
    }

    private String findWinner(Map<String, PlayerState> players) {
        return players.values().stream()
                .max((left, right) -> Integer.compare(left.score(), right.score()))
                .map(PlayerState::playerId)
                .orElse(null);
    }

    public static final class GameState {
        private final String roomId;
        private final int capacity;
        private final PartyPhase phase;
        private final int roundNumber;
        private final int turnNumber;
        private final String currentPlayerId;
        private final String hostPlayerId;
        private final Map<String, PlayerState> players;
        private final LastAction lastAction;
        private final String winner;

        public GameState(String roomId, int capacity, PartyPhase phase, int roundNumber, int turnNumber,
                         String currentPlayerId, String hostPlayerId, Map<String, PlayerState> players,
                         LastAction lastAction, String winner) {
            this.roomId = roomId;
            this.capacity = capacity;
            this.phase = phase;
            this.roundNumber = roundNumber;
            this.turnNumber = turnNumber;
            this.currentPlayerId = currentPlayerId;
            this.hostPlayerId = hostPlayerId;
            this.players = Collections.unmodifiableMap(new LinkedHashMap<>(players));
            this.lastAction = lastAction;
            this.winner = winner;
        }

        public boolean hasPlayer(String playerId) { return players.containsKey(playerId); }
        public GameState withPlayers(Map<String, PlayerState> value) { return copy(value, phase, roundNumber, turnNumber, currentPlayerId, hostPlayerId, lastAction, winner); }
        public GameState withRoom(Room room) { return new GameState(room.id(), room.capacity(), phase, roundNumber, turnNumber, currentPlayerId, hostPlayerId, players, lastAction, winner); }
        public GameState withPhase(PartyPhase value) { return copy(players, value, roundNumber, turnNumber, currentPlayerId, hostPlayerId, lastAction, winner); }
        public GameState withRoundNumber(int value) { return copy(players, phase, value, turnNumber, currentPlayerId, hostPlayerId, lastAction, winner); }
        public GameState withTurnNumber(int value) { return copy(players, phase, roundNumber, value, currentPlayerId, hostPlayerId, lastAction, winner); }
        public GameState withCurrentPlayerId(String value) { return copy(players, phase, roundNumber, turnNumber, value, hostPlayerId, lastAction, winner); }
        public GameState withHostPlayerId(String value) { return copy(players, phase, roundNumber, turnNumber, currentPlayerId, value, lastAction, winner); }
        public GameState withLastAction(LastAction value) { return copy(players, phase, roundNumber, turnNumber, currentPlayerId, hostPlayerId, value, winner); }
        public GameState withWinner(String value) { return copy(players, phase, roundNumber, turnNumber, currentPlayerId, hostPlayerId, lastAction, value); }

        private GameState copy(Map<String, PlayerState> value, PartyPhase nextPhase, int nextRoundNumber,
                               int nextTurnNumber, String nextCurrentPlayerId, String nextHostPlayerId,
                               LastAction nextLastAction, String nextWinner) {
            return new GameState(roomId, capacity, nextPhase, nextRoundNumber, nextTurnNumber,
                    nextCurrentPlayerId, nextHostPlayerId, value, nextLastAction, nextWinner);
        }

        public String roomId() { return roomId; }
        public int capacity() { return capacity; }
        public PartyPhase phase() { return phase; }
        public int roundNumber() { return roundNumber; }
        public int turnNumber() { return turnNumber; }
        public String currentPlayerId() { return currentPlayerId; }
        public String hostPlayerId() { return hostPlayerId; }
        public Map<String, PlayerState> players() { return players; }
        public LastAction lastAction() { return lastAction; }
        public String winner() { return winner; }

        public String getRoomId() { return roomId; }
        public int getCapacity() { return capacity; }
        public PartyPhase getPhase() { return phase; }
        public int getRoundNumber() { return roundNumber; }
        public int getTurnNumber() { return turnNumber; }
        public String getCurrentPlayerId() { return currentPlayerId; }
        public String getHostPlayerId() { return hostPlayerId; }
        public Map<String, PlayerState> getPlayers() { return players; }
        public LastAction getLastAction() { return lastAction; }
        public String getWinner() { return winner; }
    }

    public static final class PlayerState {
        private final String playerId;
        private final String displayName;
        private final boolean ready;
        private final int score;

        public PlayerState(String playerId, String displayName, boolean ready, int score) {
            this.playerId = playerId;
            this.displayName = displayName;
            this.ready = ready;
            this.score = score;
        }

        public PlayerState withReady(boolean value) { return new PlayerState(playerId, displayName, value, score); }
        public PlayerState withScore(int value) { return new PlayerState(playerId, displayName, ready, value); }
        public String playerId() { return playerId; }
        public String displayName() { return displayName; }
        public boolean ready() { return ready; }
        public int score() { return score; }
        public String getPlayerId() { return playerId; }
        public String getDisplayName() { return displayName; }
        public boolean isReady() { return ready; }
        public int getScore() { return score; }
    }

    public static final class LastAction {
        private final String playerId;
        private final String action;
        private final int value;

        public LastAction(String playerId, String action, int value) {
            this.playerId = playerId;
            this.action = action;
            this.value = value;
        }

        public String getPlayerId() { return playerId; }
        public String getAction() { return action; }
        public int getValue() { return value; }
    }
}
