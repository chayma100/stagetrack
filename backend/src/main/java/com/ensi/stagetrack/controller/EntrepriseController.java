package com.ensi.stagetrack.controller;

import com.ensi.stagetrack.dto.Dtos.EntrepriseDetailDto;
import com.ensi.stagetrack.dto.Dtos.EntrepriseDto;
import com.ensi.stagetrack.dto.Dtos.EntrepriseRequest;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.service.EntrepriseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin : tout le CRUD. Collaborateur : consulter et ajouter seulement (exigence du CDC).
 * Etudiant : consulter seulement.
 */
@RestController
@RequestMapping("/api/entreprises")
@PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
public class EntrepriseController {

    private final EntrepriseService service;

    public EntrepriseController(EntrepriseService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<EntrepriseDto> list(@RequestParam(required = false) String search,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return service.list(search, PageRequest.of(Math.max(page, 0), clamp(size),
                Sort.by(Sort.Direction.DESC, "id")));
    }

    @GetMapping("/{id}")
    public EntrepriseDetailDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR')")
    public ResponseEntity<EntrepriseDto> create(@Valid @RequestBody EntrepriseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntrepriseDto update(@PathVariable Long id, @Valid @RequestBody EntrepriseRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static int clamp(int size) {
        return Math.min(Math.max(size, 1), 100);
    }
}
