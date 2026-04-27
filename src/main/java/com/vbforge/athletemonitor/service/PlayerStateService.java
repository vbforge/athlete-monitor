package com.vbforge.athletemonitor.service;

import com.vbforge.athletemonitor.model.ProcessedMetric;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerStateService {

    private static final int RING_BUFFER_SIZE = 60;

    // current live state — one entry per active player
    private final ConcurrentHashMap<String, ProcessedMetric> currentState
            = new ConcurrentHashMap<>();

    // historical ring buffer — last 60 data points per player
    private final ConcurrentHashMap<String, Deque<ProcessedMetric>> history
            = new ConcurrentHashMap<>();

    // cumulative distance per player — survives window resets
    // key = playerId, value = total km since session start
    private final ConcurrentHashMap<String, Double> cumulativeDistance
            = new ConcurrentHashMap<>();

    public void update(ProcessedMetric metric) {
        String playerId = metric.getPlayerId();

        // ── accumulate distance ──────────────────────────────────────
        // distanceCovered in the incoming metric = distance in THIS window only
        // We add it to the running total
        double windowDist = metric.getDistanceCovered() != null
                ? metric.getDistanceCovered() : 0.0;

        double totalDist = cumulativeDistance.merge(
                playerId,
                windowDist,
                Double::sum
        );

        // override the metric's distanceCovered with the true cumulative value
        metric.setDistanceCovered(totalDist);

        // ── update current state ─────────────────────────────────────
        currentState.put(playerId, metric);

        // ── update ring buffer ───────────────────────────────────────
        history.computeIfAbsent(playerId,
                id -> new ArrayDeque<>(RING_BUFFER_SIZE));

        Deque<ProcessedMetric> buf = history.get(playerId);
        synchronized (buf) {
            if (buf.size() >= RING_BUFFER_SIZE) buf.pollFirst();
            buf.addLast(metric);
        }
    }

    public Map<String, ProcessedMetric> getAllPlayers() {
        return Collections.unmodifiableMap(currentState);
    }

    public List<ProcessedMetric> getHistory(String playerId) {
        Deque<ProcessedMetric> buf = history.get(playerId);
        if (buf == null) return Collections.emptyList();
        synchronized (buf) { return List.copyOf(buf); }
    }

    public void clear() {
        currentState.clear();
        history.clear();
        cumulativeDistance.clear(); // reset totals on session stop
    }
}