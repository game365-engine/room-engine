package com.roomengine.transport.websocket;

import java.io.IOException;

public interface MessageCodec {
    <T> T decode(String payload, Class<T> targetType) throws IOException;

    String encode(Object payload) throws IOException;
}
