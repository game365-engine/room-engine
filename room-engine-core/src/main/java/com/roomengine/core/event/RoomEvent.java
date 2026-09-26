package com.roomengine.core.event;

import com.roomengine.core.model.Player;
import com.roomengine.core.model.Room;

public final class RoomEvent {
    private final Type type;
    private final Room room;
    private final Player player;

    public RoomEvent(Type type, Room room, Player player) {
        this.type = type;
        this.room = room;
        this.player = player;
    }

    public Type type() { return type; }
    public Room room() { return room; }
    public Player player() { return player; }
    public Type getType() { return type; }
    public Room getRoom() { return room; }
    public Player getPlayer() { return player; }

    public enum Type { CREATED, PLAYER_JOINED, PLAYER_LEFT, CLOSED }
}
