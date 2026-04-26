package com.vbforge.athletemonitor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    /**
     *       raw-metrics: player-metrics
     *       processed-metrics: processed-metrics
     * */

    @Value("${app.kafka.topics.raw-metrics}")
    private String rawMetricsTopic;

    @Value("${app.kafka.topics.processed-metrics}")
    private String processedMatrixTopic;

    @Bean
    public NewTopic rawMetricsTopic() {
        return TopicBuilder.name(rawMetricsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic processedMatrixTopic() {
        return TopicBuilder.name(processedMatrixTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }




}
