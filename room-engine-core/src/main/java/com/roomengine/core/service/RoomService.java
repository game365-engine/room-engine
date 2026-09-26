package com.roomengine.core.service;

import com.roomengine.core.event.RoomEvent;
import com.roomengine.core.event.RoomEventListener;
import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;
import com.roomengine.core.model.Player;
import com.roomengine.core.model.Room;
import com.roomengine.core.persistence.InMemoryPersistenceAdapter;
import com.roomengine.core.persistence.PersistenceAdapter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RoomService {
    private final PersistenceAdapter persistence;
    private final List<RoomEventListener> listeners = new CopyOnWriteArrayList<>();

    public RoomService() { this(new InMemoryPersistenceAdapter()); }
    public RoomService(PersistenceAdapter persistence) { this.persistence = persistence; }

    public Room createRoom(int capacity) {
        Room room = Room.create(capacity);
        persistence.save(room);
        publish(new RoomEvent(RoomEvent.Type.CREATED, room, null));
        return room;
    }

    public Room joinRoom(String roomId, String playerId, String displayName) {
        Room room = getRoom(roomId);
        Player player = new Player(playerId, displayName, Instant.now());
        room.join(player);
        persistence.save(room);
        publish(new RoomEvent(RoomEvent.Type.PLAYER_JOINED, room, player));
        return room;
    }

    public Room leaveRoom(String roomId, String playerId) {
        Room room = getRoom(roomId);
        Player player = room.leave(playerId);
        if (player == null) return room;
        persistence.save(room);
        publish(new RoomEvent(RoomEvent.Type.PLAYER_LEFT, room, player));
        return room;
    }

    public void closeRoom(String roomId) {
        Room room = getRoom(roomId);
        room.close();
        persistence.deleteById(roomId);
        publish(new RoomEvent(RoomEvent.Type.CLOSED, room, null));
    }

    public Optional<Room> findRoom(String roomId) { return persistence.findById(roomId); }
    public void addListener(RoomEventListener listener) { listeners.add(listener); }

    private Room getRoom(String roomId) {
        return findRoom(roomId).orElseThrow(() -> new RoomException(ErrorCode.ROOM_NOT_FOUND));
    }

    private void publish(RoomEvent event) { listeners.forEach(listener -> listener.onEvent(event)); }
}
