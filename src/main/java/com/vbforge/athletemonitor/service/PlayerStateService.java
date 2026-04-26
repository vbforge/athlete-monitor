package com.vbforge.athletemonitor.service;

import com.vbforge.athletemonitor.model.ProcessedMetric;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerStateService {

    private static final int RING_BUFFER_SIZE = 60;

    //current live state — one entry per active player
    private final ConcurrentHashMap<String, ProcessedMetric> currentState = new ConcurrentHashMap<>();

    //historical ring buffer — last 60 data points per player
    private final ConcurrentHashMap<String, Deque<ProcessedMetric>> history = new ConcurrentHashMap<>();

    //update metrics
    public void update(ProcessedMetric metric) {

        String playerId = metric.getPlayerId();

        // update current state
        currentState.put(playerId, metric);

        // update ring buffer
        history.computeIfAbsent(playerId, id -> new ArrayDeque<>(RING_BUFFER_SIZE));

        Deque<ProcessedMetric> buf = history.get(playerId);
        synchronized (buf) {
            if (buf.size() >= RING_BUFFER_SIZE) {
                buf.pollFirst(); // remove oldest
            }
            buf.addLast(metric);
        }
    }

    //get all players
    /** Full snapshot — used by WebSocket broadcaster */
    public Map<String, ProcessedMetric> getAllPlayers() {
        return Collections.unmodifiableMap(currentState);
    }

    //get history
    /** Last 60 data points for one player — used by REST API history endpoint */
    public List<ProcessedMetric> getHistory(String playerId) {
        Deque<ProcessedMetric> buf = history.get(playerId);
        if (buf == null) return Collections.emptyList();
        synchronized (buf) {
            return List.copyOf(buf);
        }
    }

    //clear
    public void clear() {
        currentState.clear();
        history.clear();
    }


}
