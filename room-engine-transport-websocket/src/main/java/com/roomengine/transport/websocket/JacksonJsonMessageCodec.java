package com.roomengine.transport.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JacksonJsonMessageCodec implements MessageCodec {
    private final ObjectMapper objectMapper;

    public JacksonJsonMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T decode(String payload, Class<T> targetType) throws IOException {
        return objectMapper.readValue(payload, targetType);
    }

    @Override
    public String encode(Object payload) throws IOException {
        return objectMapper.writeValueAsString(payload);
    }
}
