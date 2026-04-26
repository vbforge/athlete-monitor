package com.vbforge.athletemonitor.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "players")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;                  // "Mohamed Salah"

    @Column(nullable = false)
    private Integer shirtNumber;          // 11

    @Column(nullable = false)
    private String position;             // "RW", "GK", "CB" …

    @Column(nullable = false)
    private String positionFull;         // "Right Wing", "Goalkeeper" …

    private Integer age;                 // affects simulation

    /**
     * 1–5 fitness rating.
     * 5 = elite athlete — high sustained speed, slow HR rise.
     * 1 = low fitness  — low top speed, fast HR rise.
     */
    private Integer fitnessLevel;

    /**
     * Natural max speed in m/s for this player.
     * Used as ceiling in the simulator.
     * Typical range: GK 6.5, CB 7.5, CM 8.0, FWD/W 9.5
     */
    private Double maxSpeed;

    /**
     * Resting heart rate. Affects HR simulation baseline.
     * Typical: 45–65 bpm for athletes.
     */
    private Integer restingHr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    @ToString.Exclude
    private Team team;
}