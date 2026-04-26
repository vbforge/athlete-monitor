package com.vbforge.athletemonitor.streams;

import com.vbforge.athletemonitor.model.RawMetric;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mutable accumulator held in the Kafka Streams state store
 * for each (playerId, 10-second window) pair.
 *
 * Must be serialisable to JSON — kept as a plain POJO.
 */
@Data
@NoArgsConstructor
public class WindowAccumulator {

    private String playerId;
    private String playerName;
    private String position;
    private String teamId;
    private int    shirtNumber;

    private double totalSpeed    = 0.0;
    private double totalHr       = 0.0;
    private int    count         = 0;
    private double maxSpeed      = 0.0;
    private double distanceCovered = 0.0; // km — approx from speed * interval
    private int    sprintCount   = 0;

    private static final double SPRINT_THRESHOLD = 7.0; // m/s

    public void addSample(RawMetric raw) {
        // capture identity fields on first sample
        if (count == 0) {
            this.playerId    = raw.getPlayerId();
            this.playerName  = raw.getPlayerName();
            this.position    = raw.getPosition();
            this.teamId      = raw.getTeamId();
            this.shirtNumber = raw.getShirtNumber() != null
                    ? raw.getShirtNumber() : 0;
        }

        totalSpeed += raw.getSpeed();
        totalHr    += raw.getHeartRate();
        count++;

        if (raw.getSpeed() > maxSpeed) {
            maxSpeed = raw.getSpeed();
        }

        // distance: speed (m/s) × 1 second interval → convert to km
        distanceCovered += raw.getSpeed() * 1.0 / 1000.0;

        // count sprint entries (crossing threshold, not sustained)
        if (raw.getSpeed() >= SPRINT_THRESHOLD) {
            sprintCount++;
        }
    }


}
