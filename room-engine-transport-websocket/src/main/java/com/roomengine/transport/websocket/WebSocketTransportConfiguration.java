package com.roomengine.transport.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketTransportConfiguration implements WebSocketConfigurer {
    private final ObjectProvider<RoomWebSocketHandler> roomWebSocketHandlerProvider;

    public WebSocketTransportConfiguration(ObjectProvider<RoomWebSocketHandler> roomWebSocketHandlerProvider) {
        this.roomWebSocketHandlerProvider = roomWebSocketHandlerProvider;
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageCodec messageCodec(ObjectMapper objectMapper) { return new JacksonJsonMessageCodec(objectMapper); }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionAuthenticator connectionAuthenticator() { return session -> true; }

    @Bean
    public RoomWebSocketHandler roomWebSocketHandler(MessageCodec messageCodec,
                                                      ConnectionAuthenticator connectionAuthenticator) {
        return new RoomWebSocketHandler(messageCodec, connectionAuthenticator);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(roomWebSocketHandlerProvider.getObject(), "/ws/rooms")
                .setAllowedOriginPatterns("*");
    }
}
