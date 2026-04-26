package com.vbforge.athletemonitor.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.athletemonitor.model.ProcessedMetric;
import com.vbforge.athletemonitor.model.RawMetric;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MetricStreamProcessor {

    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topics.raw-metrics}")
    private String rawMetricsTopic;

    @Value("${app.kafka.topics.processed-metrics}")
    private String processedMetricsTopic;

    /**
     * Topology of the flow:
     *  1. Read RawMetric JSON strings from player-metrics
     *  2. Group by playerId (already the Kafka message key)
     *  3. Apply 10-second tumbling window
     *  4. Aggregate: sum speed + HR, count messages
     *  5. Map to ProcessedMetric with averages
     *  6. Write JSON strings to processed-metrics
     */
    @Bean
    public KStream<String, String> athleteMetricStream(StreamsBuilder builder) {

        //1) source stream
        KStream<String, String> rawStreamMetrics = builder.stream(rawMetricsTopic, Consumed.with(Serdes.String(), Serdes.String()));

        //2), 3), 4) window aggregation
        rawStreamMetrics
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10)))
                .aggregate(
                        // initialiser
                        WindowAccumulator::new,

                        // aggregator — adds each raw message to the window bucket
                        (playerId, rawJson, acc) -> {
                            try {
                                RawMetric raw = objectMapper.readValue(rawJson, RawMetric.class);
                                acc.addSample(raw);
                            } catch (Exception e) {
                                log.warn("Failed to deserialise RawMetric: {}", e.getMessage());
                            }
                            return acc;
                        },

                        // materialised store — keeps window state
                        Materialized.with(Serdes.String(),
                                new WindowAccumulatorSerde())
                )

                //5) map windowed result to ProcessedMetric
                .toStream()
                .map((windowedKey, acc) -> {
                    if (acc == null || acc.getCount() == 0) return null;

                    ProcessedMetric processed = ProcessedMetric.builder()
                            .playerId(acc.getPlayerId())
                            .playerName(acc.getPlayerName())
                            .position(acc.getPosition())
                            .teamId(acc.getTeamId())
                            .shirtNumber(acc.getShirtNumber())
                            .avgSpeed10s(round(acc.getTotalSpeed() / acc.getCount()))
                            .avgHr10s(round(acc.getTotalHr() / acc.getCount()))
                            .maxSpeedSeen(round(acc.getMaxSpeed()))
                            .distanceCovered(round(acc.getDistanceCovered()))
                            .sprintCount(acc.getSprintCount())
                            .loadPercent(round(calculateLoad(acc)))
                            .timestamp(System.currentTimeMillis())
                            .build();

                    try {
                        String json = objectMapper.writeValueAsString(processed);
                        return new org.apache.kafka.streams.KeyValue<>(
                                windowedKey.key(), json);
                    } catch (Exception e) {
                        log.warn("Failed to serialise ProcessedMetric: {}", e.getMessage());
                        return null;
                    }
                })
                .filter((k, v) -> k != null && v != null)

                //6) sink to processed-metrics topic
                .to(processedMetricsTopic,
                        Produced.with(Serdes.String(), Serdes.String()));

        return rawStreamMetrics;

    }



    //helpers

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    /**
     * Load % = weighted combination of speed ratio and HR ratio.
     * Gives a single 0–100 fatigue/intensity score per player.
     */
    private double calculateLoad(WindowAccumulator acc) {
        if (acc.getCount() == 0) return 0;
        double avgSpeed = acc.getTotalSpeed() / acc.getCount();
        double avgHr    = acc.getTotalHr()    / acc.getCount();
        double speedRatio = Math.min(avgSpeed / 10.0, 1.0);
        double hrRatio    = Math.min((avgHr - 50) / 165.0, 1.0);
        return (speedRatio * 0.5 + hrRatio * 0.5) * 100.0;
    }


}












