package com.vbforge.athletemonitor.controller;

import com.vbforge.athletemonitor.model.Player;
import com.vbforge.athletemonitor.model.Team;
import com.vbforge.athletemonitor.repository.PlayerRepository;
import com.vbforge.athletemonitor.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TeamAdminRestController {

    private final TeamRepository   teamRepository;
    private final PlayerRepository playerRepository;

    // ── TEAMS ──────────────────────────────────────────────────────

    @GetMapping("/teams")
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @PostMapping("/teams")
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        team.setId(null); // ensure insert, not update
        return ResponseEntity.ok(teamRepository.save(team));
    }

    @PutMapping("/teams/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id,
                                           @RequestBody Team body) {
        return teamRepository.findById(id).map(team -> {
            team.setName(body.getName());
            team.setFormation(body.getFormation());
            team.setBadgeColor(body.getBadgeColor());
            team.setCountry(body.getCountry());
            return ResponseEntity.ok(teamRepository.save(team));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/teams/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        if (!teamRepository.existsById(id))
            return ResponseEntity.notFound().build();
        teamRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── PLAYERS ────────────────────────────────────────────────────

    @GetMapping("/teams/{teamId}/players")
    public ResponseEntity<List<Player>> getPlayers(@PathVariable Long teamId) {
        return teamRepository.findById(teamId)
                .map(t -> ResponseEntity.ok(t.getPlayers()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/teams/{teamId}/players")
    public ResponseEntity<Player> addPlayer(@PathVariable Long teamId,
                                            @RequestBody Player player) {
        return teamRepository.findById(teamId).map(team -> {
            player.setId(null);
            player.setTeam(team);
            return ResponseEntity.ok(playerRepository.save(player));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id,
                                               @RequestBody Player body) {
        return playerRepository.findById(id).map(player -> {
            player.setName(body.getName());
            player.setShirtNumber(body.getShirtNumber());
            player.setPosition(body.getPosition());
            player.setPositionFull(body.getPositionFull());
            player.setAge(body.getAge());
            player.setFitnessLevel(body.getFitnessLevel());
            player.setMaxSpeed(body.getMaxSpeed());
            player.setRestingHr(body.getRestingHr());
            return ResponseEntity.ok(playerRepository.save(player));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/players/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        if (!playerRepository.existsById(id))
            return ResponseEntity.notFound().build();
        playerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
