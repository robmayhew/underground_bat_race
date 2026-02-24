package com.underground.batrace.handler;

import com.underground.batrace.service.GameSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;

@Component
public class GameWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private final GameSessionService gameSessionService;

    public GameWebSocketHandler(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Client connected: {}", session.getId());

        String gameId = extractQueryParam(session.getHandshakeInfo().getUri(), "gameId");
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        gameSessionService.registerPlayer(session.getId(), gameId, sink);

        // Inbound: process client messages; on completion/error unregister the player
        Mono<Void> inbound = session.receive()
                .doOnNext(msg -> gameSessionService.handleMessage(session.getId(), msg.getPayloadAsText()))
                .doFinally(sig -> {
                    log.info("Client disconnected: {} ({})", session.getId(), sig);
                    gameSessionService.unregisterPlayer(session.getId());
                    sink.tryEmitComplete();
                })
                .then();

        // Outbound: stream messages pushed by the game session service
        Mono<Void> outbound = session.send(
                sink.asFlux().map(session::textMessage)
        );

        // Run both concurrently; closing the inbound will complete the sink, ending outbound
        return Mono.zip(inbound, outbound).then();
    }

    private String extractQueryParam(URI uri, String name) {
        String query = uri.getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && name.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
