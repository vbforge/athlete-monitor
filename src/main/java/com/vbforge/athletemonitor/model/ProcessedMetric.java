package com.vbforge.athletemonitor.model;

import lombok.*;

/**
 * Produced by Kafka Streams processor → topic: processed-metrics
 * Contains 10-second window averages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedMetric {

    private String playerId;
    private String playerName;
    private String position;
    private String teamId;
    private Double avgSpeed10s;     // 10-second window average
    private Double avgHr10s;        // 10-second window average
    private Double maxSpeedSeen;    // running max since monitoring started
    private Double distanceCovered; // approx km (accumulated)
    private Integer sprintCount;    // times speed crossed 7.0 m/s
    private Double loadPercent;     // fatigue estimate 0–100
    private Long timestamp;
}