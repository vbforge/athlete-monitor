package com.vbforge.athletemonitor.controller;

import com.vbforge.athletemonitor.model.Player;
import com.vbforge.athletemonitor.model.ProcessedMetric;
import com.vbforge.athletemonitor.model.Team;
import com.vbforge.athletemonitor.repository.TeamRepository;
import com.vbforge.athletemonitor.service.ActiveSessionService;
import com.vbforge.athletemonitor.service.PlayerStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricsRestController {

    private final PlayerStateService   playerStateService;
    private final ActiveSessionService sessionService;
    private final TeamRepository       teamRepository;

    /** Current live snapshot of all active players */
    @GetMapping("/players")
    public ResponseEntity<Map<String, ProcessedMetric>> getPlayers() {

        return ResponseEntity.ok(playerStateService.getAllPlayers());

    }

    /** Last 60 data points for chart history */
    @GetMapping("/history/{playerId}")
    public ResponseEntity<List<ProcessedMetric>> getHistory(@PathVariable String playerId) {

        return ResponseEntity.ok(playerStateService.getHistory(playerId));
    }

    /** All teams with their squads */
    @GetMapping("/teams")
    public ResponseEntity<List<Team>> getTeams() {

        return ResponseEntity.ok(teamRepository.findAll());
    }

    /** Start monitoring a team with selected player IDs */
    @PostMapping("/session/start")
    public ResponseEntity<String> startSession(@RequestBody SessionStartRequest request) {

        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new RuntimeException("Team not found: " + request.teamId()));

        List<Player> selected = team.getPlayers().stream()
                .filter(p -> request.playerIds().contains(p.getId()))
                .toList();

        if (selected.size() != 11) {
            return ResponseEntity.badRequest().body("Exactly 11 players must be selected. Got: " + selected.size());
        }

        playerStateService.clear();
        sessionService.startSession(team, selected);

        return ResponseEntity.ok("Session started for " + team.getName());
    }

    /** Stop monitoring */
    @PostMapping("/session/stop")
    public ResponseEntity<String> stopSession() {

        sessionService.stopSession();
        playerStateService.clear();

        return ResponseEntity.ok("Session stopped");
    }

    /** Request body for session start */
    record SessionStartRequest(Long teamId, List<Long> playerIds) {}
}