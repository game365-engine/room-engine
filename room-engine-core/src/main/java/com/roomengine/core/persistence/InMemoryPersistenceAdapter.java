package com.roomengine.core.persistence;

import com.roomengine.core.model.Room;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class InMemoryPersistenceAdapter implements PersistenceAdapter {
    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();

    @Override public void save(Room room) { rooms.put(room.id(), room); }
    @Override public Optional<Room> findById(String roomId) { return Optional.ofNullable(rooms.get(roomId)); }
    @Override public void deleteById(String roomId) { rooms.remove(roomId); }
}
