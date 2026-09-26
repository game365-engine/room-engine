package com.roomengine.core.model;

import java.time.Instant;

public final class Session {
    private final String id;
    private final String playerId;
    private final Instant connectedAt;

    public Session(String id, String playerId, Instant connectedAt) {
        if (id == null || id.isBlank() || playerId == null || playerId.isBlank() || connectedAt == null) {
            throw new IllegalArgumentException("session fields must be present");
        }
        this.id = id;
        this.playerId = playerId;
        this.connectedAt = connectedAt;
    }

    public String id() { return id; }
    public String playerId() { return playerId; }
    public Instant connectedAt() { return connectedAt; }
    public String getId() { return id; }
    public String getPlayerId() { return playerId; }
    public Instant getConnectedAt() { return connectedAt; }
}
