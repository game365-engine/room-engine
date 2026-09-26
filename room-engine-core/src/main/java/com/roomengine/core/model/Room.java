package com.roomengine.core.model;

import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class Room {
    private final String id;
    private final int capacity;
    private final Instant createdAt;
    private final Map<String, Player> players = new LinkedHashMap<>();
    private RoomStatus status = RoomStatus.CREATED;

    public Room(String id, int capacity, Instant createdAt) {
        if (id == null || id.isBlank() || capacity < 1 || createdAt == null) {
            throw new RoomException(ErrorCode.INVALID_ARGUMENT);
        }
        this.id = id;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public static Room create(int capacity) {
        return new Room(UUID.randomUUID().toString(), capacity, Instant.now());
    }

    public synchronized void join(Player player) {
        ensureOpen();
        if (players.containsKey(player.id())) {
            throw new RoomException(ErrorCode.PLAYER_ALREADY_IN_ROOM);
        }
        if (players.size() >= capacity) {
            throw new RoomException(ErrorCode.ROOM_FULL);
        }
        players.put(player.id(), player);
        status = RoomStatus.ACTIVE;
    }

    public synchronized Player leave(String playerId) {
        Player player = players.remove(playerId);
        if (player != null && players.isEmpty()) {
            status = RoomStatus.CLOSING;
        }
        return player;
    }

    public synchronized void close() {
        status = RoomStatus.CLOSED;
        players.clear();
    }

    private void ensureOpen() {
        if (status == RoomStatus.CLOSING || status == RoomStatus.CLOSED) {
            throw new RoomException(ErrorCode.ROOM_NOT_ACCEPTING_PLAYERS);
        }
    }

    public String id() { return id; }
    public int capacity() { return capacity; }
    public Instant createdAt() { return createdAt; }
    public synchronized RoomStatus status() { return status; }
    public synchronized Map<String, Player> players() { return Collections.unmodifiableMap(new LinkedHashMap<>(players)); }
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public Instant getCreatedAt() { return createdAt; }
    public synchronized RoomStatus getStatus() { return status; }
    public synchronized Map<String, Player> getPlayers() { return players(); }
}
