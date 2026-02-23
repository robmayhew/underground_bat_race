package com.underground.batrace.handler;

import com.underground.batrace.service.PeriodicMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class GameWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private final PeriodicMessageService periodicMessageService;

    public GameWebSocketHandler(PeriodicMessageService periodicMessageService) {
        this.periodicMessageService = periodicMessageService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Client connected: {}", session.getId());

        // Register session for periodic messages
        periodicMessageService.registerSession(session.getId());

        // Echo messages from client
        Flux<WebSocketMessage> echoMessages = session.receive()
                .doOnNext(msg -> log.info("[{}] received: {}", session.getId(), msg.getPayloadAsText()))
                .map(WebSocketMessage::getPayloadAsText)
                .map(payload -> session.textMessage("echo: " + payload));

        // Periodic messages from service
        Flux<WebSocketMessage> periodicMessages = periodicMessageService
                .getMessageStream(session.getId())
                .map(session::textMessage);

        // Merge both streams
        return session.send(
                Flux.merge(echoMessages, periodicMessages)
                        .doFinally(sig -> {
                            log.info("Client disconnected: {} ({})", session.getId(), sig);
                            periodicMessageService.unregisterSession(session.getId());
                        })
        );
    }
}
