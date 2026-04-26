package com.vbforge.athletemonitor.service;

import com.vbforge.athletemonitor.model.Player;
import com.vbforge.athletemonitor.model.Team;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ActiveSessionService {

    //current monitor team (if null - no session active)
    private final AtomicReference<Team> activeTeam = new AtomicReference<>(null);

    //selected 11 players for this active match
    private final CopyOnWriteArrayList<Player> activePlayers = new CopyOnWriteArrayList<>();

    //how many minutes into the match (simulating fatigue based on match continuing)
    @Getter
    private volatile int matchMinute = 0;

    //start session to monitoring data for the team and players
    public void startSession(Team team, List<Player> selectedPlayers) {
        activeTeam.set(team);
        activePlayers.clear();
        activePlayers.addAll(selectedPlayers);
        matchMinute = 0;
        log.info("Session started for team: {}, with players: {}", team.getName(), selectedPlayers.size());
    }

    //stop session
    public void stopSession() {
        log.info("Session stopped for team: {}", activeTeam.get() != null ? activeTeam.get().getName() : "none");
        activeTeam.set(null);
        activePlayers.clear();
        matchMinute = 0;
    }

    public void incrementMatchMinute() {
        matchMinute++;
    }

    public boolean isActive(){
        return activeTeam.get() != null && !activePlayers.isEmpty();
    }

    public Team getActiveTeam() {
        return activeTeam.get();
    }

    public List<Player> getActivePlayers() {
        return Collections.unmodifiableList(activePlayers);
    }


}

















