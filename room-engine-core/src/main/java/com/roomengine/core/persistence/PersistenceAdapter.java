package com.roomengine.core.persistence;

import com.roomengine.core.model.Room;

import java.util.Optional;

public interface PersistenceAdapter {
    void save(Room room);
    Optional<Room> findById(String roomId);
    void deleteById(String roomId);
}
