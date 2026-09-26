package com.roomengine.core.event;

@FunctionalInterface
public interface RoomEventListener {
    void onEvent(RoomEvent event);
}
