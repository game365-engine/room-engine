package com.roomengine.examples.party;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomengine.core.service.RoomService;
import com.roomengine.transport.websocket.JacksonJsonMessageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PartyWebSocketHandlerTest {
    private PartyGameService gameService;
    private PartyWebSocketHandler handler;

    @BeforeEach
    void setUp() {
        gameService = new PartyGameService(new RoomService());
        handler = new PartyWebSocketHandler(gameService, new JacksonJsonMessageCodec(new ObjectMapper()));
    }

    @Test
    void broadcastsOnlyToSessionsInSameRoomAndUsesConnectionIdentity() throws Exception {
        String roomId = gameService.createGame(4).roomId();
        gameService.joinPlayer(roomId, "p1", "Alice");
        gameService.joinPlayer(roomId, "p2", "Bob");
        String otherRoomId = gameService.createGame(4).roomId();
        gameService.joinPlayer(otherRoomId, "p3", "Carol");

        TestWebSocketSession firstSession = session("s1", roomId, "p1");
        TestWebSocketSession secondSession = session("s2", roomId, "p2");
        TestWebSocketSession otherRoomSession = session("s3", otherRoomId, "p3");
        handler.afterConnectionEstablished(firstSession);
        handler.afterConnectionEstablished(secondSession);
        handler.afterConnectionEstablished(otherRoomSession);
        firstSession.clearMessages();
        secondSession.clearMessages();
        otherRoomSession.clearMessages();

        handler.handleTextMessage(firstSession,
                new TextMessage("{\"type\":\"ready\",\"playerId\":\"p2\",\"roomId\":\"" + otherRoomId + "\"}"));

        assertEquals(1, firstSession.messages().size());
        assertEquals(1, secondSession.messages().size());
        assertEquals(0, otherRoomSession.messages().size());
        assertTrue(gameService.getGame(roomId).players().get("p1").ready());
        assertFalse(gameService.getGame(roomId).players().get("p2").ready());
    }

    private TestWebSocketSession session(String sessionId, String roomId, String playerId) {
        return new TestWebSocketSession(sessionId, URI.create(
                "ws://localhost/ws/party?roomId=" + roomId + "&playerId=" + playerId));
    }

    private static final class TestWebSocketSession implements WebSocketSession {
        private final String id;
        private final URI uri;
        private final List<WebSocketMessage<?>> messages = new ArrayList<>();
        private boolean open = true;

        private TestWebSocketSession(String id, URI uri) {
            this.id = id;
            this.uri = uri;
        }

        @Override public String getId() { return id; }
        @Override public URI getUri() { return uri; }
        @Override public HttpHeaders getHandshakeHeaders() { return new HttpHeaders(); }
        @Override public Map<String, Object> getAttributes() { return Map.of(); }
        @Override public Principal getPrincipal() { return null; }
        @Override public InetSocketAddress getLocalAddress() { return null; }
        @Override public InetSocketAddress getRemoteAddress() { return null; }
        @Override public String getAcceptedProtocol() { return ""; }
        @Override public void setTextMessageSizeLimit(int messageSize) { }
        @Override public int getTextMessageSizeLimit() { return 0; }
        @Override public void setBinaryMessageSizeLimit(int messageSize) { }
        @Override public int getBinaryMessageSizeLimit() { return 0; }
        @Override public List<WebSocketExtension> getExtensions() { return List.of(); }
        @Override public void sendMessage(WebSocketMessage<?> message) { messages.add(message); }
        @Override public boolean isOpen() { return open; }
        @Override public void close() { open = false; }
        @Override public void close(CloseStatus status) { open = false; }
        private List<WebSocketMessage<?>> messages() { return messages; }
        private void clearMessages() { messages.clear(); }
    }
}
