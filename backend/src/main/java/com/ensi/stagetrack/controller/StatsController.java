package com.ensi.stagetrack.controller;

import com.ensi.stagetrack.dto.Dtos.StatsDto;
import com.ensi.stagetrack.service.StatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public StatsDto stats() {
        return service.compute();
    }
}
