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
    private Integer shirtNumber;
    private Double avgSpeed10s;
    private Double avgHr10s;
    private Double maxSpeedSeen;
    private Double distanceCovered;
    private Integer sprintCount;
    private Double loadPercent;
    private Long timestamp;
}