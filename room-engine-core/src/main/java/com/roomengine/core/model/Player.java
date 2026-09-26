package com.roomengine.core.model;

import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;

import java.time.Instant;

import java.util.Objects;

public final class Player {
    private final String id;
    private final String displayName;
    private final Instant joinedAt;

    public Player(String id, String displayName, Instant joinedAt) {
        if (id == null || id.isBlank()) {
            throw new RoomException(ErrorCode.INVALID_ARGUMENT);
        }
        if (displayName == null || displayName.isBlank()) {
            throw new RoomException(ErrorCode.INVALID_ARGUMENT);
        }
        if (joinedAt == null) {
            throw new RoomException(ErrorCode.INVALID_ARGUMENT);
        }
        this.id = id;
        this.displayName = displayName;
        this.joinedAt = joinedAt;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public Instant joinedAt() { return joinedAt; }
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Instant getJoinedAt() { return joinedAt; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Player player)) return false;
        return id.equals(player.id) && displayName.equals(player.displayName) && joinedAt.equals(player.joinedAt);
    }

    @Override
    public int hashCode() { return Objects.hash(id, displayName, joinedAt); }

    @Override
    public String toString() { return "Player[id=" + id + ", displayName=" + displayName + ", joinedAt=" + joinedAt + "]"; }
}
