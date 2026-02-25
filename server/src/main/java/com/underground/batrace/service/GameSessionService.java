package com.underground.batrace.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.underground.batrace.model.GameSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {

    private static final Logger log = LoggerFactory.getLogger(GameSessionService.class);

    // gameId -> GameSession
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    // sessionId -> gameId
    private final Map<String, String> sessionToGameId = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    /**
     * Called when a new WebSocket connection is established.
     * Assigns the player to a game (existing if gameId is provided and not full, otherwise new).
     * Sends a "config" message to the new player and broadcasts "player_joined" to existing players.
     */
    public void registerPlayer(String sessionId, String requestedGameId, Sinks.Many<String> sink) {
        GameSession session = resolveSession(requestedGameId);

        int playerId = session.addPlayer(sessionId, sink);
        sessionToGameId.put(sessionId, session.getGameId());

        // Notify existing players that someone joined
        List<Integer> existingPlayers = session.getOtherPlayerIds(sessionId);
        if (!existingPlayers.isEmpty()) {
            ObjectNode joined = objectMapper.createObjectNode();
            joined.put("type", "player_joined");
            joined.put("playerId", playerId);
            joined.put("playerCount", session.getPlayerCount());
            send(session, null, joined, true, sessionId);
        }

        // Send config to the new player
        ObjectNode config = objectMapper.createObjectNode();
        config.put("type", "config");
        config.put("gameId", session.getGameId());
        config.put("seed", session.getSeed());
        config.put("playerId", playerId);
        ArrayNode existingArray = config.putArray("existingPlayers");
        existingPlayers.forEach(existingArray::add);
        send(session, sessionId, config, false, null);

        log.info("Player {} joined game {} as playerId={} ({} total)",
                sessionId, session.getGameId(), playerId, session.getPlayerCount());
    }

    /**
     * Called when the client sends a message.
     * Supports type="state": relays the player's game state to all other players in the game.
     */
    public void handleMessage(String sessionId, String message) {
        String gameId = sessionToGameId.get(sessionId);
        if (gameId == null) return;
        GameSession session = sessions.get(gameId);
        if (session == null) return;
        Integer playerId = session.getPlayerId(sessionId);
        if (playerId == null) return;

        try {
            JsonNode node = objectMapper.readTree(message);
            String type = node.path("type").asText();

            if ("state".equals(type)) {
                ObjectNode relay = objectMapper.createObjectNode();
                relay.put("type", "player_state");
                relay.put("playerId", playerId);
                relay.set("data", node.path("data"));
                session.broadcastExcept(sessionId, objectMapper.writeValueAsString(relay));
            }
        } catch (Exception e) {
            log.error("Error handling message from session {}: {}", sessionId, message, e);
        }
    }

    /**
     * Called when a WebSocket connection closes.
     * Removes the player from their game and broadcasts "player_left" to remaining players.
     */
    public void unregisterPlayer(String sessionId) {
        String gameId = sessionToGameId.remove(sessionId);
        if (gameId == null) return;
        GameSession session = sessions.get(gameId);
        if (session == null) return;

        int playerId = session.removePlayer(sessionId);

        if (session.isEmpty()) {
            sessions.remove(gameId);
            log.info("Game {} removed (no players remaining)", gameId);
        } else {
            ObjectNode left = objectMapper.createObjectNode();
            left.put("type", "player_left");
            left.put("playerId", playerId);
            left.put("playerCount", session.getPlayerCount());
            send(session, null, left, true, null);
        }

        log.info("Player {} left game {} (playerId={})", sessionId, gameId, playerId);
    }

    // --- helpers ---

    private GameSession resolveSession(String requestedGameId) {
        if (requestedGameId != null) {
            GameSession existing = sessions.get(requestedGameId);
            if (existing != null && !existing.isFull()) {
                return existing;
            }
        }
        return createNewSession();
    }

    private GameSession createNewSession() {
        String gameId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        long seed = random.nextLong();
        seed = 7;
        GameSession session = new GameSession(gameId, seed);
        sessions.put(gameId, session);
        log.info("Created new game session: {} seed={}", gameId, seed);
        return session;
    }

    /** Sends a JSON node to either a single session or broadcasts (optionally excluding one session). */
    private void send(GameSession session, String targetSessionId, ObjectNode node,
                      boolean broadcast, String excludeSessionId) {
        try {
            String json = objectMapper.writeValueAsString(node);
            if (broadcast) {
                if (excludeSessionId != null) {
                    session.broadcastExcept(excludeSessionId, json);
                } else {
                    session.broadcast(json);
                }
            } else if (targetSessionId != null) {
                session.sendTo(targetSessionId, json);
            }
        } catch (Exception e) {
            log.error("Error serializing message", e);
        }
    }
}
