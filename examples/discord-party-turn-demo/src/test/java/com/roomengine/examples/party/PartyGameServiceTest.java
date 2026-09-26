package com.roomengine.examples.party;

import com.roomengine.core.exception.RoomException;
import com.roomengine.core.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartyGameServiceTest {
    private PartyGameService service;
    private String roomId;

    @BeforeEach
    void setUp() {
        service = new PartyGameService(new RoomService());
        roomId = service.createGame(4).roomId();
        service.joinPlayer(roomId, "p1", "Alice");
        service.joinPlayer(roomId, "p2", "Bob");
    }

    @Test
    void requiresAllPlayersReadyAndHostToStart() {
        assertEquals(PartyErrorCode.NOT_READY,
                assertThrows(PartyGameException.class, () -> service.startGame(roomId, "p1")).getErrorCode());
        service.markReady(roomId, "p1");
        service.markReady(roomId, "p2");
        assertEquals(PartyErrorCode.NOT_HOST,
                assertThrows(PartyGameException.class, () -> service.startGame(roomId, "p2")).getErrorCode());
        assertEquals(PartyPhase.PLAYING, service.startGame(roomId, "p1").phase());
    }

    @Test
    void validatesCurrentPlayerAndFinishesAfterThreeRounds() {
        service.markReady(roomId, "p1");
        service.markReady(roomId, "p2");
        service.startGame(roomId, "p1");

        assertEquals(PartyErrorCode.NOT_YOUR_TURN,
                assertThrows(PartyGameException.class, () -> service.takeTurn(roomId, "p2", "roll")).getErrorCode());

        for (int turn = 0; turn < 6; turn++) {
            String playerId = service.getGame(roomId).currentPlayerId();
            service.takeTurn(roomId, playerId, "pass");
        }

        assertEquals(PartyPhase.FINISHED, service.getGame(roomId).phase());
        assertEquals(3, service.getGame(roomId).roundNumber());
        assertEquals(6, service.getGame(roomId).turnNumber());
        assertEquals(PartyErrorCode.GAME_FINISHED,
                assertThrows(PartyGameException.class, () -> service.takeTurn(roomId, "p1", "pass")).getErrorCode());
    }

    @Test
    void enforcesRoomCapacityAndMembership() {
        service.joinPlayer(roomId, "p3", "Carol");
        service.joinPlayer(roomId, "p4", "Dan");
        assertThrows(RoomException.class, () -> service.joinPlayer(roomId, "p5", "Eve"));
        assertEquals(PartyErrorCode.PLAYER_NOT_IN_ROOM,
                assertThrows(PartyGameException.class, () -> service.markReady(roomId, "stranger")).getErrorCode());
    }

    @Test
    void closesRoomAndRemovesPartyState() {
        service.closeGame(roomId);

        assertEquals(PartyErrorCode.ROOM_NOT_FOUND,
                assertThrows(PartyGameException.class, () -> service.getGame(roomId)).getErrorCode());
        assertEquals(PartyErrorCode.ROOM_NOT_FOUND,
                assertThrows(PartyGameException.class, () -> service.joinPlayer(roomId, "p3", "Carol")).getErrorCode());
    }
}
