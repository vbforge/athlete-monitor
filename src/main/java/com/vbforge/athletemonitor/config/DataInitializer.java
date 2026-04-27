package com.vbforge.athletemonitor.config;

//import com.vbforge.athletemonitor.service.ActiveSessionService;

import com.vbforge.athletemonitor.model.Player;
import com.vbforge.athletemonitor.model.Team;
import com.vbforge.athletemonitor.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.event.ContextRefreshedEvent;
//import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TeamRepository teamRepository;
//    private final ActiveSessionService sessionService;       //this we need only for demo when testing autoStartSession and web-socket

    @Override
    public void run(String... args) {
        if (teamRepository.count() > 0) {
            log.info("Teams already seeded — skipping DataInitializer");
            return;
        }
        log.info("Seeding teams and squads...");
        teamRepository.saveAll(List.of(
                buildLiverpool(),
                buildManCity(),
                buildRealMadrid(),
                buildInterMilan(),
                buildBayernMunich(),
                buildManUnited()
        ));
        log.info("Seeded 6 teams successfully");
    }

    // ── MANCHESTER UNITED ── 4-4-2 ────────────────────────────────────────────
    private Team buildManUnited() {
        Team t = Team.builder()
                .name("Manchester United")
                .formation("4-4-2")
                .badgeColor("#DA291C")
                .country("England")
                .build();
        addPlayers(t, List.of(
                p("André Onana",        24, "GK", "Goalkeeper",    29, 4, 6.9,  50),
                p("Diogo Dalot",        20, "RB", "Right Back",    26, 4, 8.1,  60),
                p("Raphaël Varane",     19, "CB", "Center Back",   31, 4, 7.4,  52),
                p("Lisandro Martínez",  6,  "CB", "Center Back",   27, 5, 7.8,  53),
                p("Luke Shaw",          23, "LB", "Left Back",     29, 4, 7.6,  54),
                p("Bruno Fernandes",    8,  "RM", "Right Mid",     30, 5, 8.2,  58),
                p("Casemiro",           18, "CM", "Center Mid",    33, 4, 6.5,  51),
                p("Kobbie Mainoo",      37, "CM", "Center Mid",    20, 5, 8.4,  57),
                p("Marcus Rashford",    10, "LM", "Left Mid",      27, 4, 9.0,  59),
                p("Rasmus Højlund",     11, "ST", "Striker",       22, 4, 8.8,  55),
                p("Joshua Zirkzee",     9,  "ST", "Striker",       23, 4, 8.5,  54)
        ));
        return t;
    }

    // ── BAYERN MUNICH ── 4-2-3-1 ─────────────────────────────────────────────
    private Team buildBayernMunich() {
        Team t = Team.builder()
                .name("Bayern Munich")
                .formation("4-2-3-1")
                .badgeColor("#DC052D")
                .country("Germany")
                .build();
        addPlayers(t, List.of(
                p("Manuel Neuer",        1,  "GK", "Goalkeeper",    38, 5, 6.5,  48),
                p("Joshua Kimmich",      6,  "RB", "Right Back",    29, 5, 7.9,  57),
                p("Matthijs de Ligt",    4,  "CB", "Center Back",   25, 5, 7.6,  52),
                p("Dayot Upamecano",     2,  "CB", "Center Back",   26, 4, 8.0,  53),
                p("Alphonso Davies",     19, "LB", "Left Back",     24, 5, 9.6,  58),
                p("Leon Goretzka",       8,  "CDM","Def Mid",       29, 4, 7.5,  55),
                p("Konrad Laimer",       27, "CDM","Def Mid",       27, 4, 8.3,  56),
                p("Jamal Musiala",       42, "CAM","Att Mid",       22, 5, 9.5,  57),
                p("Leroy Sané",          10, "RW", "Right Wing",    28, 4, 9.2,  59),
                p("Kingsley Coman",      11, "LW", "Left Wing",     28, 4, 9.1,  58),
                p("Harry Kane",          9,  "ST", "Striker",       31, 5, 8.6,  50)
        ));
        return t;
    }

    // ── LIVERPOOL FC ── 4-3-3 ──────────────────────────────────────────
    private Team buildLiverpool() {
        Team t = Team.builder()
            .name("Liverpool FC")
            .formation("4-3-3")
            .badgeColor("#C8102E")
            .country("England")
            .build();
        addPlayers(t, List.of(
            p("Alisson Becker",    1,  "GK", "Goalkeeper",    31, 5, 6.5,  52),
            p("Trent Alexander",   66, "RB", "Right Back",    25, 5, 8.5,  55),
            p("Virgil van Dijk",   4,  "CB", "Center Back",   32, 4, 7.8,  58),
            p("Ibrahima Konaté",   5,  "CB", "Center Back",   25, 5, 8.0,  54),
            p("Andy Robertson",    26, "LB", "Left Back",     30, 5, 8.8,  56),
            p("Alexis Mac Allister",10,"CM", "Center Mid",    25, 5, 8.2,  57),
            p("Dominik Szoboszlai",8,  "CM", "Center Mid",    23, 5, 8.6,  55),
            p("Curtis Jones",      17, "CM", "Center Mid",    23, 4, 8.3,  58),
            p("Mohamed Salah",     11, "RW", "Right Wing",    32, 5, 9.8,  53),
            p("Luis Díaz",         7,  "LW", "Left Wing",     27, 5, 9.5,  54),
            p("Darwin Núñez",      9,  "ST", "Striker",       24, 4, 9.2,  57)
        ));
        return t;
    }

    // ── MAN CITY ── 4-3-3 ─────────────────────────────────────────────
    private Team buildManCity() {
        Team t = Team.builder()
            .name("Manchester City")
            .formation("4-3-3")
            .badgeColor("#6CABDD")
            .country("England")
            .build();
        addPlayers(t, List.of(
            p("Ederson",           31, "GK", "Goalkeeper",    30, 5, 6.8,  50),
            p("Kyle Walker",       2,  "RB", "Right Back",    34, 4, 8.2,  60),
            p("Rúben Dias",        3,  "CB", "Center Back",   27, 5, 7.9,  55),
            p("Manuel Akanji",     25, "CB", "Center Back",   29, 5, 8.1,  54),
            p("Josko Gvardiol",    24, "LB", "Left Back",     22, 5, 8.9,  53),
            p("Rodri",             16, "CM", "Center Mid",    28, 5, 7.8,  56),
            p("Kevin De Bruyne",   17, "CM", "Center Mid",    33, 4, 8.4,  58),
            p("Bernardo Silva",    20, "CM", "Center Mid",    29, 5, 8.7,  54),
            p("Phil Foden",        47, "RW", "Right Wing",    24, 5, 9.3,  55),
            p("Jeremy Doku",       11, "LW", "Left Wing",     22, 5, 9.7,  54),
            p("Erling Haaland",    9,  "ST", "Striker",       24, 5, 9.4,  52)
        ));
        return t;
    }

    // ── REAL MADRID ── 4-3-3 ──────────────────────────────────────────
    private Team buildRealMadrid() {
        Team t = Team.builder()
            .name("Real Madrid")
            .formation("4-3-3")
            .badgeColor("#FEBE10")
            .country("Spain")
            .build();
        addPlayers(t, List.of(
            p("Thibaut Courtois",  1,  "GK", "Goalkeeper",    32, 5, 6.6,  53),
            p("Dani Carvajal",     2,  "RB", "Right Back",    32, 4, 8.3,  57),
            p("Éder Militão",      3,  "CB", "Center Back",   26, 5, 8.0,  54),
            p("Antonio Rüdiger",   22, "CB", "Center Back",   31, 4, 8.2,  56),
            p("Ferland Mendy",     23, "LB", "Left Back",     29, 4, 8.6,  55),
            p("Aurélien Tchouaméni",18,"CM", "Center Mid",    24, 5, 8.3,  55),
            p("Toni Kroos",        8,  "CM", "Center Mid",    34, 3, 7.5,  60),
            p("Luka Modric",       10, "CM", "Center Mid",    38, 3, 7.2,  62),
            p("Vinícius Júnior",   7,  "LW", "Left Wing",     23, 5, 9.9,  52),
            p("Jude Bellingham",   5,  "RW", "Right Wing",    21, 5, 9.1,  53),
            p("Kylian Mbappé",     9,  "ST", "Striker",       25, 5, 10.5, 50)
        ));
        return t;
    }

    // ── INTER MILAN ── 3-5-2 ──────────────────────────────────────────
    private Team buildInterMilan() {
        Team t = Team.builder()
            .name("Inter Milan")
            .formation("3-5-2")
            .badgeColor("#0068A8")
            .country("Italy")
            .build();
        addPlayers(t, List.of(
            p("Yann Sommer",       1,  "GK", "Goalkeeper",    35, 4, 6.4,  55),
            p("Benjamin Pavard",   28, "CB", "Center Back",   28, 4, 7.9,  57),
            p("Francesco Acerbi",  15, "CB", "Center Back",   36, 3, 7.0,  62),
            p("Alessandro Bastoni",95, "CB", "Center Back",   25, 5, 8.2,  54),
            p("Denzel Dumfries",   2,  "RWB","Right Wing Back",28,5, 9.0,  55),
            p("Nicolò Barella",    23, "CM", "Center Mid",    27, 5, 8.8,  54),
            p("Hakan Çalhanoğlu",  20, "CM", "Center Mid",    30, 5, 8.0,  56),
            p("Henrikh Mkhitaryan",22, "CM", "Center Mid",    35, 3, 7.4,  61),
            p("Federico Dimarco",  32, "LWB","Left Wing Back", 26, 5, 9.1, 53),
            p("Lautaro Martínez",  10, "ST", "Striker",       26, 5, 9.0,  54),
            p("Marcus Thuram",     9,  "ST", "Striker",       26, 5, 9.3,  53)
        ));
        return t;
    }

    // ── helpers ────────────────────────────────────────────────────────
    private Player p(String name, int shirt, String pos, String posFull,
                     int age, int fitness, double maxSpeed, int restingHr) {
        return Player.builder()
            .name(name).shirtNumber(shirt)
            .position(pos).positionFull(posFull)
            .age(age).fitnessLevel(fitness)
            .maxSpeed(maxSpeed).restingHr(restingHr)
            .build();
    }

    private void addPlayers(Team team, List<Player> players) {
        players.forEach(p -> {
            p.setTeam(team);
            team.getPlayers().add(p);
        });
    }

    //removed - since we use actual data from kafka now
//    @EventListener(ContextRefreshedEvent.class)
//    @Transactional
//    public void autoStartSession() {
//        // small delay to let DataInitializer.run() finish seeding first
//        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
//
//        teamRepository.findByName("Liverpool FC").ifPresent(team -> {
//            // activate all 11 players for demo purposes
//            sessionService.startSession(team, team.getPlayers());
//            log.info("Auto-started monitoring session for: {}", team.getName());
//        });
//    }

}