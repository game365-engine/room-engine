package com.roomengine.examples.party;

import com.roomengine.api.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping({"/api/v1/party"})
public final class PartyController {
    private final PartyGameService gameService;
    private final PartyWebSocketHandler webSocketHandler;

    public PartyController(PartyGameService gameService, PartyWebSocketHandler webSocketHandler) {
        this.gameService = gameService;
        this.webSocketHandler = webSocketHandler;
    }

    @PostMapping("/rooms")
    public Result<PartyGameService.GameState> create(@RequestParam(defaultValue = "8") int capacity) {
        return Result.success(gameService.createGame(capacity));
    }

    @PostMapping("/rooms/{roomId}/players")
    public Result<PartyGameService.GameState> join(
            @PathVariable String roomId,
            @RequestBody JoinRequest request) {
        PartyGameService.GameState state = gameService.joinPlayer(roomId, request.getPlayerId(), request.getDisplayName());
        broadcast(roomId, state);
        return Result.success(state);
    }

    @DeleteMapping("/rooms/{roomId}/players/{playerId}")
    public Result<PartyGameService.GameState> leave(
            @PathVariable String roomId,
            @PathVariable String playerId) {
        PartyGameService.GameState state = gameService.leavePlayer(roomId, playerId);
        broadcast(roomId, state);
        return Result.success(state);
    }

    @DeleteMapping("/rooms/{roomId}")
    public Result<Void> close(@PathVariable String roomId) {
        gameService.closeGame(roomId);
        return Result.success(null);
    }

    @GetMapping("/rooms/{roomId}")
    public Result<PartyGameService.GameState> get(@PathVariable String roomId) {
        return Result.success(gameService.getGame(roomId));
    }

    private void broadcast(String roomId, PartyGameService.GameState state) {
        try {
            webSocketHandler.broadcastGameState(roomId, state);
        } catch (IOException exception) {
            throw new IllegalStateException("failed to broadcast party game state", exception);
        }
    }

    public static final class JoinRequest {
        private String playerId;
        private String displayName;

        public JoinRequest() {
        }

        public String getPlayerId() {
            return playerId;
        }

        public void setPlayerId(String playerId) {
            this.playerId = playerId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }
}
