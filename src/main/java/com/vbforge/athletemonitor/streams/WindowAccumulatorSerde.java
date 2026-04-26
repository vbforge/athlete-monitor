package com.vbforge.athletemonitor.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Custom Serde (Serialiser + Deserialiser) for WindowAccumulator.
 * Used by Kafka Streams to persist window state between ticks.
 */
public class WindowAccumulatorSerde implements Serde<WindowAccumulator> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Serializer<WindowAccumulator> serializer() {
        return (topic, data) -> {
            try {
                return MAPPER.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("WindowAccumulator serialise failed", e);
            }
        };
    }

    @Override
    public Deserializer<WindowAccumulator> deserializer() {
        return (topic, data) -> {
            if (data == null) return new WindowAccumulator();
            try {
                return MAPPER.readValue(data, WindowAccumulator.class);
            } catch (Exception e) {
                return new WindowAccumulator();
            }
        };
    }
}