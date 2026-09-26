package com.roomengine.transport.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class RoomWebSocketHandler extends TextWebSocketHandler {
    private final MessageCodec messageCodec;
    private final ConnectionAuthenticator connectionAuthenticator;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    public RoomWebSocketHandler(MessageCodec messageCodec, ConnectionAuthenticator connectionAuthenticator) {
        this.messageCodec = messageCodec;
        this.connectionAuthenticator = connectionAuthenticator;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        if (!connectionAuthenticator.authenticate(session)) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessions.put(session.getId(), session);
        roomId(session).ifPresent(roomId -> roomSessions.computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>())
                .put(session.getId(), session));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<?, ?> payload = messageCodec.decode(message.getPayload(), Map.class);
        if ("ping".equals(payload.get("type"))) {
            send(session, Map.of("type", "pong", "timestamp", Instant.now().toString()));
            return;
        }

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("type", "message");
        envelope.put("from", session.getId());
        envelope.put("payload", payload);
        java.util.Optional<String> room = roomId(session);
        if (room.isPresent()) {
            broadcastToRoom(room.get(), envelope);
        } else {
            broadcast(envelope);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        roomId(session).ifPresent(roomId -> {
            ConcurrentMap<String, WebSocketSession> members = roomSessions.get(roomId);
            if (members != null) {
                members.remove(session.getId());
                if (members.isEmpty()) roomSessions.remove(roomId, members);
            }
        });
    }

    public void broadcastToRoom(String roomId, Object payload) throws IOException {
        String json = messageCodec.encode(payload);
        ConcurrentMap<String, WebSocketSession> members = roomSessions.get(roomId);
        if (members == null) return;
        for (WebSocketSession candidate : members.values()) {
            if (candidate.isOpen()) candidate.sendMessage(new TextMessage(json));
        }
    }

    private java.util.Optional<String> roomId(WebSocketSession session) {
        if (session.getUri() == null) return java.util.Optional.empty();
        String query = session.getUri().getQuery();
        if (query == null) return java.util.Optional.empty();
        for (String parameter : query.split("&")) {
            String[] parts = parameter.split("=", 2);
            if (parts.length == 2 && "roomId".equals(parts[0])) {
                return java.util.Optional.of(URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return java.util.Optional.empty();
    }

    private void broadcast(Object payload) throws IOException {
        String json = messageCodec.encode(payload);
        for (WebSocketSession candidate : sessions.values()) {
            if (candidate.isOpen()) {
                candidate.sendMessage(new TextMessage(json));
            }
        }
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(messageCodec.encode(payload)));
    }
}
