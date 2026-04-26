package com.vbforge.athletemonitor.model;

import lombok.*;

/**
 * Fired by AlertService when thresholds are breached.
 * Broadcast to WebSocket topic /topic/alerts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    public enum Level { INFO, WARNING, CRITICAL }

    private String playerId;
    private String playerName;
    private Level level;
    private String message;          // "High intensity: 7.2 m/s / 182 bpm"
    private Long timestamp;
}