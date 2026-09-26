package com.roomengine.examples.basic;

import com.roomengine.api.Result;
import com.roomengine.core.model.Room;
import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;
import com.roomengine.core.service.RoomService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @PostMapping
    public Result<Room> create(@RequestParam(defaultValue = "8") int capacity) {
        return Result.success(roomService.createRoom(capacity));
    }

    @PostMapping("/{roomId}/players")
    public Result<Room> join(@PathVariable String roomId, @RequestBody JoinRequest request) {
        return Result.success(roomService.joinRoom(roomId, request.getPlayerId(), request.getDisplayName()));
    }

    @DeleteMapping("/{roomId}/players/{playerId}")
    public Result<Room> leave(@PathVariable String roomId, @PathVariable String playerId) {
        return Result.success(roomService.leaveRoom(roomId, playerId));
    }

    @DeleteMapping("/{roomId}")
    public Result<Void> close(@PathVariable String roomId) {
        roomService.closeRoom(roomId);
        return Result.success(null);
    }

    @GetMapping("/{roomId}")
    public Result<Room> get(@PathVariable String roomId) {
        return Result.success(roomService.findRoom(roomId).orElseThrow(
                () -> new RoomException(ErrorCode.ROOM_NOT_FOUND)));
    }

    public static class JoinRequest {
        private String playerId;
        private String displayName;

        public JoinRequest() { }

        public String getPlayerId() { return playerId; }
        public void setPlayerId(String playerId) { this.playerId = playerId; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
