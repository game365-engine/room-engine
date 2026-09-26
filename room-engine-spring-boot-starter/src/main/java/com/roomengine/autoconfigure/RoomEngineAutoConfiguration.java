package com.roomengine.autoconfigure;

import com.roomengine.api.ApiExceptionHandler;
import com.roomengine.core.service.RoomService;
import com.roomengine.transport.websocket.WebSocketTransportConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ApiExceptionHandler.class, WebSocketTransportConfiguration.class})
public class RoomEngineAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public RoomService roomService() {
        return new RoomService();
    }

}
