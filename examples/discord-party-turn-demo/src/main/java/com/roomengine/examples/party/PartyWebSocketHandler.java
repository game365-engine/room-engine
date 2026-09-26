package com.roomengine.examples.party;

import com.roomengine.core.exception.RoomException;
import com.roomengine.transport.websocket.MessageCodec;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PartyWebSocketHandler extends TextWebSocketHandler {
    private static final String ROOM_ID = "roomId";
    private static final String PLAYER_ID = "playerId";

    private final PartyGameService gameService;
    private final MessageCodec codec;
    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public PartyWebSocketHandler(PartyGameService gameService, MessageCodec codec) {
        this.gameService = gameService;
        this.codec = codec;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Map<String, String> query = queryParameters(session);
        String roomId = query.get(ROOM_ID);
        String playerId = query.get(PLAYER_ID);
        try {
            gameService.ensurePlayer(roomId, playerId);
        } catch (PartyGameException | RoomException exception) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        connections.put(session.getId(), new Connection(roomId, playerId));
        roomSessions.computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>()).put(session.getId(), session);
        send(session, Map.of("type", "game_state", "state", gameService.getGame(roomId)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Connection connection = connections.get(session.getId());
        if (connection == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        try {
            Map<?, ?> command = codec.decode(message.getPayload(), Map.class);
            String type = String.valueOf(command.get("type"));
            if ("ping".equals(type)) {
                send(session, Map.of("type", "pong", "timestamp", Instant.now().toString()));
                return;
            }

            PartyGameService.GameState state = switch (type) {
                case "ready" -> gameService.markReady(connection.roomId(), connection.playerId());
                case "start_game" -> gameService.startGame(connection.roomId(), connection.playerId());
                case "turn_action" -> gameService.takeTurn(
                        connection.roomId(), connection.playerId(), String.valueOf(command.get("action")));
                default -> throw new PartyGameException(PartyErrorCode.INVALID_ACTION);
            };
            broadcast(connection.roomId(), Map.of("type", "game_state", "state", state));
        } catch (PartyGameException exception) {
            sendError(session, exception.getErrorCode().name(), exception.getErrorCode().getMessage());
        } catch (RoomException exception) {
            sendError(session, exception.getErrorCode().name(), exception.getErrorCode().getMessage());
        } catch (RuntimeException exception) {
            sendError(session, "INVALID_MESSAGE", "invalid message");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Connection connection = connections.remove(session.getId());
        if (connection == null) {
            return;
        }
        ConcurrentMap<String, WebSocketSession> sessions = roomSessions.get(connection.roomId());
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                roomSessions.remove(connection.roomId(), sessions);
            }
        }
    }

    private Map<String, String> queryParameters(WebSocketSession session) {
        if (session.getUri() == null) {
            return Map.of();
        }
        Map<String, String> parameters = new ConcurrentHashMap<>();
        UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams()
                .forEach((key, values) -> {
                    if (!values.isEmpty()) {
                        parameters.put(key, values.getFirst());
                    }
                });
        return parameters;
    }

    public void broadcastGameState(String roomId, PartyGameService.GameState state) throws IOException {
        broadcast(roomId, Map.of("type", "game_state", "state", state));
    }

    private void broadcast(String roomId, Object payload) throws IOException {
        String json = codec.encode(payload);
        ConcurrentMap<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession candidate : sessions.values()) {
            if (candidate.isOpen()) {
                candidate.sendMessage(new TextMessage(json));
            }
        }
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(codec.encode(payload)));
    }

    private void sendError(WebSocketSession session, String code, String message) throws IOException {
        send(session, Map.of("type", "error", "code", code, "message", message));
    }

    static final class Connection {
        private final String roomId;
        private final String playerId;

        private Connection(String roomId, String playerId) {
            this.roomId = roomId;
            this.playerId = playerId;
        }

        private String roomId() {
            return roomId;
        }

        private String playerId() {
            return playerId;
        }
    }
}
