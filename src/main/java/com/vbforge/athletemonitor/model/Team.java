package com.vbforge.athletemonitor.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;                  // "Liverpool FC"

    @Column(nullable = false)
    private String formation;             // "4-3-3", "4-4-2", "3-5-2" …

    private String badgeColor;            // hex, e.g. "#C8102E" for Liverpool red

    private String country;               // "England"

    @OneToMany(mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)  //to avoid LAZY INITIALIZATION
    @Builder.Default
    private List<Player> players = new ArrayList<>();


}