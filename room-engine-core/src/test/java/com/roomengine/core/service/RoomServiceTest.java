package com.roomengine.core.service;

import com.roomengine.core.exception.ErrorCode;
import com.roomengine.core.exception.RoomException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest {
    @Test
    void createsRoomAndManagesPlayers() {
        RoomService service = new RoomService();
        var room = service.createRoom(2);

        service.joinRoom(room.id(), "p1", "Alice");
        service.joinRoom(room.id(), "p2", "Bob");

        assertEquals(2, service.findRoom(room.id()).orElseThrow().players().size());
        RoomException fullRoomException = assertThrows(
                RoomException.class, () -> service.joinRoom(room.id(), "p3", "Carol"));
        assertEquals(ErrorCode.ROOM_FULL, fullRoomException.getErrorCode());

        service.leaveRoom(room.id(), "p1");
        assertEquals(1, service.findRoom(room.id()).orElseThrow().players().size());
    }

    @Test
    void closesRoomAndRemovesItFromPersistence() {
        RoomService service = new RoomService();
        var room = service.createRoom(2);

        service.closeRoom(room.id());

        assertTrue(service.findRoom(room.id()).isEmpty());
        RoomException missingRoomException = assertThrows(
                RoomException.class, () -> service.joinRoom(room.id(), "p1", "Alice"));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, missingRoomException.getErrorCode());
    }
}
