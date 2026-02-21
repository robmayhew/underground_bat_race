package com.underground.batrace.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class GameWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Client connected: {}", session.getId());

        return session.send(
                session.receive()
                        .doOnNext(msg -> log.info("[{}] received: {}", session.getId(), msg.getPayloadAsText()))
                        .map(WebSocketMessage::getPayloadAsText)
                        .map(payload -> session.textMessage("echo: " + payload))
                        .doFinally(sig -> log.info("Client disconnected: {} ({})", session.getId(), sig))
        );
    }
}
