package com.roomengine.transport.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonJsonMessageCodecTest {
    private final MessageCodec codec = new JacksonJsonMessageCodec(new ObjectMapper());

    @Test
    void encodesAndDecodesJsonMessages() throws Exception {
        String json = codec.encode(Map.of("type", "ping"));
        Map<?, ?> message = codec.decode(json, Map.class);

        assertEquals("ping", message.get("type"));
    }
}
