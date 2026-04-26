package com.vbforge.athletemonitor.service;

import com.vbforge.athletemonitor.model.Alert;
import com.vbforge.athletemonitor.model.ProcessedMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SimpMessagingTemplate messagingTemplate;

    //cooldown: don't re-fire the same alert for a player within 15 seconds
    private final Map<String, Long> lastAlertTime = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 15_000;

    // thresholds
    private static final double CRITICAL_SPEED = 6.5;
    private static final double CRITICAL_HR    = 175.0;
    private static final double WARNING_SPEED  = 5.5;
    private static final double WARNING_HR     = 158.0;


    public void evaluate(ProcessedMetric metric) {
        Alert.Level level = determineLevel(metric);
        if (level == null) return;

        String playerId = metric.getPlayerId();
        long now = System.currentTimeMillis();

        // cooldown check — avoid alert spam for the same player
        Long last = lastAlertTime.get(playerId);
        if (last != null && (now - last) < COOLDOWN_MS) return;

        lastAlertTime.put(playerId, now);

        Alert alert = Alert.builder()
                .playerId(playerId)
                .playerName(metric.getPlayerName())
                .level(level)
                .message(buildMessage(level, metric))
                .timestamp(now)
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", alert);
        log.info("Alert fired [{}}] for {} — {}", level, metric.getPlayerName(), alert.getMessage());
    }


    private Alert.Level determineLevel(ProcessedMetric m) {
        if (m.getAvgSpeed10s() >= CRITICAL_SPEED
                && m.getAvgHr10s() >= CRITICAL_HR) {
            return Alert.Level.CRITICAL;
        }
        if (m.getAvgSpeed10s() >= WARNING_SPEED
                || m.getAvgHr10s() >= WARNING_HR) {
            return Alert.Level.WARNING;
        }
        return null;
    }

    private String buildMessage(Alert.Level level, ProcessedMetric m) {
        return String.format("%s — %s: %.1f m/s · %d bpm",
                level == Alert.Level.CRITICAL ? "🔴 HIGH INTENSITY" : "🟡 ELEVATED",
                m.getPlayerName(),
                m.getAvgSpeed10s(),
                m.getAvgHr10s().intValue());
    }


}
