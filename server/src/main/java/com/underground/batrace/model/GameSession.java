package com.underground.batrace.model;

import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameSession {

    private static final int MAX_PLAYERS = 4;

    private final String gameId;
    private final long seed;

    // sessionId -> assigned playerId (1-4)
    private final Map<String, Integer> sessionToPlayerId = new ConcurrentHashMap<>();
    // sessionId -> outbound sink
    private final Map<String, Sinks.Many<String>> sessionToSink = new ConcurrentHashMap<>();
    // which player IDs are in use
    private final Set<Integer> usedPlayerIds = Collections.synchronizedSet(new HashSet<>());

    public GameSession(String gameId, long seed) {
        this.gameId = gameId;
        this.seed = seed;
    }

    /**
     * Adds a player to the session. Returns the assigned playerId (1-4), or -1 if the game is full.
     */
    public synchronized int addPlayer(String sessionId, Sinks.Many<String> sink) {
        if (sessionToPlayerId.size() >= MAX_PLAYERS) return -1;
        int playerId = 1;
        while (usedPlayerIds.contains(playerId)) playerId++;
        usedPlayerIds.add(playerId);
        sessionToPlayerId.put(sessionId, playerId);
        sessionToSink.put(sessionId, sink);
        return playerId;
    }

    /**
     * Removes a player. Returns their playerId, or -1 if not found.
     */
    public synchronized int removePlayer(String sessionId) {
        Integer playerId = sessionToPlayerId.remove(sessionId);
        if (playerId != null) {
            usedPlayerIds.remove(playerId);
            sessionToSink.remove(sessionId);
            return playerId;
        }
        return -1;
    }

    /** Returns a snapshot of currently connected player IDs (excluding the given sessionId). */
    public List<Integer> getOtherPlayerIds(String excludeSessionId) {
        List<Integer> ids = new ArrayList<>();
        sessionToPlayerId.forEach((sid, pid) -> {
            if (!sid.equals(excludeSessionId)) ids.add(pid);
        });
        return ids;
    }

    public boolean isFull() {
        return sessionToPlayerId.size() >= MAX_PLAYERS;
    }

    public boolean isEmpty() {
        return sessionToPlayerId.isEmpty();
    }

    public int getPlayerCount() {
        return sessionToPlayerId.size();
    }

    public Integer getPlayerId(String sessionId) {
        return sessionToPlayerId.get(sessionId);
    }

    public String getGameId() {
        return gameId;
    }

    public long getSeed() {
        return seed;
    }

    /** Sends a message to every player in the game. */
    public void broadcast(String message) {
        sessionToSink.forEach((id, sink) -> sink.tryEmitNext(message));
    }

    /** Sends a message to every player except the given session. */
    public void broadcastExcept(String sessionId, String message) {
        sessionToSink.forEach((id, sink) -> {
            if (!id.equals(sessionId)) sink.tryEmitNext(message);
        });
    }

    /** Sends a message to one specific player. */
    public void sendTo(String sessionId, String message) {
        Sinks.Many<String> sink = sessionToSink.get(sessionId);
        if (sink != null) sink.tryEmitNext(message);
    }

    /** Completes the sink for a session (call on disconnect). */
    public void completeSink(String sessionId) {
        Sinks.Many<String> sink = sessionToSink.get(sessionId);
        if (sink != null) sink.tryEmitComplete();
    }
}
