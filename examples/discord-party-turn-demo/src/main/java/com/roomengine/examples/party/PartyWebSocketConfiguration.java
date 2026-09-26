package com.roomengine.examples.party;

import com.roomengine.transport.websocket.MessageCodec;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class PartyWebSocketConfiguration implements WebSocketConfigurer {
    private final org.springframework.beans.factory.ObjectProvider<PartyWebSocketHandler> handlerProvider;

    public PartyWebSocketConfiguration(org.springframework.beans.factory.ObjectProvider<PartyWebSocketHandler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    @org.springframework.context.annotation.Bean
    public PartyWebSocketHandler partyWebSocketHandler(PartyGameService gameService, MessageCodec codec) {
        return new PartyWebSocketHandler(gameService, codec);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handlerProvider.getObject(), "/ws/party").setAllowedOriginPatterns("*");
    }
}
