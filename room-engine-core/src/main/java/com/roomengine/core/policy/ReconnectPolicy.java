package com.roomengine.core.policy;

public interface ReconnectPolicy {
    void onHeartbeat(String playerId);
    void onDisconnected(String playerId);
}
