package com.vbforge.athletemonitor.simulator;

import com.vbforge.athletemonitor.model.Player;
import com.vbforge.athletemonitor.model.RawMetric;
import com.vbforge.athletemonitor.service.ActiveSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a very important component of app.
 * Every second it generates a RawMetric for each active player, shaped by their profile:
 *   - Age — players over 30 get a speed penalty that grows with age;
 *   - Fitness — controls how fast HR climbs under load;
 *   - Position — GKs have low movement with occasional bursts; wingers have high variance;
 *   - Match fatigue (tiredness, exhaustion) — speed and HR drift realistically over 90 minutes;
 * */

@Component
@Slf4j
@RequiredArgsConstructor
public class MetricSimulator {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ActiveSessionService sessionService;

    @Value("${app.kafka.topics.raw-metrics}")
    private String rowMetricsTopic;

    private final Random random = new Random();

    //tracks per-player states between ticks
    //key=player DB id; value=current simulated speed (for smooth transition between ticks)
    private final ConcurrentHashMap<Long, Double> currentSpeed = new ConcurrentHashMap<>();

    //key=player DB id; value=current simulated heart-rate
    private final ConcurrentHashMap<Long, Double> currentHr = new ConcurrentHashMap<>();

    //increments match minute counter every 60 ticks (60 seconds)
    private int ticksCounter = 0;

    //simulator method
    //send generated random data into kafka (simulating players are active and doing actions)
    @Scheduled(fixedRateString = "${app.simulation.interval-ms}")
    public void simulate(){

        if(!sessionService.isActive()){
            return; //no active sessions checker
        }

        ticksCounter++;
        if(ticksCounter % 60 == 0){
            sessionService.incrementMatchMinute();
        }

        int matchMinute = sessionService.getMatchMinute();

        for(Player player : sessionService.getActivePlayers()){
            //generate
            RawMetric metric = generateMetric(player, matchMinute);
            //send
            kafkaTemplate.send(rowMetricsTopic, String.valueOf(player.getId()), metric);
        }
    }

    //metrics generator method (core simulation logic)
    private RawMetric generateMetric(Player player, int matchMinute){

        double speed = nextSpeed(player, matchMinute);
        int heartRate = nextHeartRate(player, speed, matchMinute);

        return RawMetric.builder()
                .playerId(String.valueOf(player.getId()))
                .playerName(player.getName())
                .position(player.getPosition())
                .teamId(String.valueOf(player.getTeam().getId()))
                .shirtNumber(player.getShirtNumber())
                .speed(speed)
                .heartRate(heartRate)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    //helper methods

    /**
     * Generates the next speed value using a random walk anchored to
     * the player's profile. Key influences:
     *
     *  - maxSpeed ceiling (from player profile)
     *  - position behavior pattern (GK vs winger vs CB etc.)
     *  - age penalty (>30 years old reduces effective ceiling)
     *  - match fatigue (speed decays ~12% by minute 90)
     */
    private double nextSpeed(Player player, int matchMinute){

        double prev = currentSpeed.getOrDefault(player.getId(), baseSpeedForPosition(player.getPosition()) * 0.5);

        //age penalty (>30 age -1% per year)
        double agePenalty = player.getAge() > 30
                ? 1.0 - (player.getAge() - 30) * 0.01
                : 1.0;

        //fatigue - liner decay, -12% by minute 90
        double fatigue = 1.0 - ((double) matchMinute / 90) * 0.12;

        double effectiveCeiling = player.getMaxSpeed() * agePenalty * fatigue;

        // position determines how volatile speed changes are
        double volatility = volatilityForPosition(player.getPosition());

        //random walk: move toward a target speed
        double target = targetSpeed(player.getPosition(), effectiveCeiling);

        double next = prev + (target - prev) * 0.3 + (random.nextDouble() - 0.5) * volatility;

        next = Math.max(0.0, Math.min(next, effectiveCeiling));

        currentSpeed.put(player.getId(), next);

        return Math.round(next * 10.0) / 10.0;

    }

    /**
     * Heart rate driven by:
     *  - current speed (higher speed → higher HR)
     *  - fitness level (lower fitness → HR climbs faster)
     *  - age (older players have higher resting HR under load)
     *  - match fatigue (HR baseline drifts up over 90 min)
     */
    private int nextHeartRate(Player player, double speed, int matchMinute) {
        double prev = currentHr.getOrDefault(player.getId(),
                (double) player.getRestingHr() + 20);

        // how much this speed load pushes HR up
        // fitness 5 = efficient, fitness 1 = inefficient
        double fitnessEfficiency = 0.6 + (player.getFitnessLevel() / 5.0) * 0.4;
        double speedLoad = (speed / player.getMaxSpeed()) * (200 - player.getRestingHr());

        // match fatigue adds a baseline HR drift (up to +15 bpm by min 90)
        double fatigueDrift = (matchMinute / 90.0) * 15.0;

        double target = player.getRestingHr()
                + speedLoad / fitnessEfficiency
                + fatigueDrift;

        // smooth transition
        double next = prev + (target - prev) * 0.25
                + (random.nextDouble() - 0.5) * 4.0;

        next = Math.max(player.getRestingHr(),
                Math.min(next, 215.0));

        currentHr.put(player.getId(), next);
        return (int) Math.round(next);
    }


    //position behavior helpers methods

    /**
     * Base cruising speed per position.
     * GKs barely move; wingers are almost always in motion.
     */
    private double baseSpeedForPosition(String position) {
        return switch (position) {
            case "GK"  -> 1.0;
            case "CB"  -> 3.5;
            case "LB", "RB", "LWB", "RWB" -> 4.5;
            case "CM"  -> 4.0;
            case "LW", "RW" -> 5.5;
            case "ST"  -> 5.0;
            default    -> 4.0;
        };
    }

    /**
     * How much speed jumps tick-to-tick.
     * GKs are very stable; wingers are chaotic.
     */
    private double volatilityForPosition(String position) {
        return switch (position) {
            case "GK"  -> 0.3;
            case "CB"  -> 0.6;
            case "LB", "RB", "LWB", "RWB" -> 1.0;
            case "CM"  -> 0.9;
            case "LW", "RW" -> 1.5;
            case "ST"  -> 1.2;
            default    -> 1.0;
        };
    }

    /**
     * Target speed this tick — occasionally spikes to simulate sprints,
     * otherwise returns to cruising speed for the position.
     */
    private double targetSpeed(String position, double ceiling) {
        // 8% chance of a sprint burst
        boolean sprinting = random.nextDouble() < 0.08;
        if (sprinting) {
            return ceiling * (0.75 + random.nextDouble() * 0.25);
        }
        // GK: 5% chance of sudden burst (goal kick run etc.)
        if (position.equals("GK") && random.nextDouble() < 0.05) {
            return ceiling * 0.6;
        }
        return baseSpeedForPosition(position)
                * (0.7 + random.nextDouble() * 0.3);
    }


}






