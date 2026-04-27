package com.vbforge.athletemonitor.controller;

import com.vbforge.athletemonitor.repository.TeamRepository;
import com.vbforge.athletemonitor.service.ActiveSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final TeamRepository teamRepository;
    private final ActiveSessionService sessionService;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("activeTeam", sessionService.getActiveTeam());
        return "dashboard";
    }
}