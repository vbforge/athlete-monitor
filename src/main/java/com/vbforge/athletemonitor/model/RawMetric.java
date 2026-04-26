package com.vbforge.athletemonitor.model;

import lombok.*;

/**
 * Produced by MetricSimulator → Kafka topic: player-metrics
 * Plain POJO — not a JPA entity (lives only in Kafka, not MySQL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMetric {

    private String playerId;       // "11" (shirt number as string key)
    private String playerName;
    private String position;
    private String teamId;
    private Integer shirtNumber;
    private Double speed;          // m/s  (0 – player.maxSpeed)
    private Integer heartRate;     // bpm  (restingHr – 210)
    private Double latitude;       // pitch coordinate (optional, Phase 5)
    private Double longitude;
    private Long timestamp;        // System.currentTimeMillis()
}