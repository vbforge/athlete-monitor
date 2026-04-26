package com.vbforge.athletemonitor.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vbforge.athletemonitor.model.ProcessedMetric;
import com.vbforge.athletemonitor.service.AlertService;
import com.vbforge.athletemonitor.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessedMetricConsumer {

    private final PlayerStateService playerStateService;
    private final AlertService alertService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
        topics           = "${app.kafka.topics.processed-metrics}",
        groupId          = "${spring.kafka.consumer.group-id}",
        // override global deserialiser — we want raw String here,
        // not the JsonDeserialiser from application.yml
        properties       = {
            "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "key.deserializer=org.apache.kafka.common.serialization.StringDeserializer"
        }
    )
    public void consume(@Payload String message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
            ProcessedMetric metric = objectMapper
                    .readValue(message, ProcessedMetric.class);

            if (metric.getPlayerId() == null || metric.getAvgSpeed10s() == null) {
                log.warn("Skipping incomplete metric from topic {}", topic);
                return;
            }

            playerStateService.update(metric);
            alertService.evaluate(metric);

        } catch (Exception e) {
            log.warn("Failed to process metric message: {} — {}",
                    e.getMessage(), message);
        }
    }
}