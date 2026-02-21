package com.underground.batrace.config;

import com.underground.batrace.handler.GameWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public HandlerMapping webSocketHandlerMapping(GameWebSocketHandler gameWebSocketHandler) {
        return new SimpleUrlHandlerMapping(
                Map.of("/ws/game", gameWebSocketHandler),
                -1 // higher priority than default handler
        );
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
