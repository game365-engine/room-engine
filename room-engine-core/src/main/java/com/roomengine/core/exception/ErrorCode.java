package com.roomengine.core.exception;

public enum ErrorCode {
    SUCCESS(200, "success"),
    INVALID_ARGUMENT(1000, "invalid argument"),
    ROOM_NOT_FOUND(1001, "room not found"),
    ROOM_FULL(1002, "room is full"),
    PLAYER_ALREADY_IN_ROOM(1003, "player is already in the room"),
    ROOM_NOT_ACCEPTING_PLAYERS(1004, "room is not accepting players"),
    PLAYER_NOT_FOUND(1005, "player not found");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
