package com.roomengine.transport.websocket;

import org.springframework.web.socket.WebSocketSession;

@FunctionalInterface
public interface ConnectionAuthenticator {
    boolean authenticate(WebSocketSession session);
}
