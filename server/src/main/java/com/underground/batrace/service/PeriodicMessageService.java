package com.underground.batrace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PeriodicMessageService {

    private static final Logger log = LoggerFactory.getLogger(PeriodicMessageService.class);
    private final Map<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();

    public PeriodicMessageService() {
        startPeriodicBroadcast();
    }

    public void registerSession(String sessionId) {
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, sink);
        log.info("Session registered: {}", sessionId);
    }

    public void unregisterSession(String sessionId) {
        Sinks.Many<String> sink = sessionSinks.remove(sessionId);
        if (sink != null) {
            sink.tryEmitComplete();
            log.info("Session unregistered: {}", sessionId);
        }
    }

    public Flux<String> getMessageStream(String sessionId) {
        Sinks.Many<String> sink = sessionSinks.get(sessionId);
        if (sink != null) {
            return sink.asFlux();
        }
        return Flux.empty();
    }

    private void startPeriodicBroadcast() {
        Flux.interval(Duration.ofSeconds(10))
                .doOnNext(tick -> {
                    String message = "Periodic message at tick: " + tick;
                    log.info("Broadcasting to {} sessions: {}", sessionSinks.size(), message);
                    sessionSinks.forEach((sessionId, sink) -> {
                        sink.tryEmitNext(message);
                    });
                })
                .subscribe();
    }
}
