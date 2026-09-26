package com.roomengine.examples.party;

public enum PartyErrorCode {
    ROOM_NOT_FOUND("room not found"),
    PLAYER_NOT_IN_ROOM("player is not in the room"),
    GAME_ALREADY_STARTED("game has already started"),
    NOT_READY("all players must be ready"),
    NOT_ENOUGH_PLAYERS("not enough players"),
    NOT_HOST("only the host can start the game"),
    NOT_YOUR_TURN("it is not your turn"),
    INVALID_ACTION("invalid action"),
    GAME_FINISHED("game has finished");

    private final String message;

    PartyErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
