package com.ensi.stagetrack.controller;

import com.ensi.stagetrack.dto.Dtos.CandidatureDto;
import com.ensi.stagetrack.dto.Dtos.CandidatureRequest;
import com.ensi.stagetrack.dto.Dtos.OffreDto;
import com.ensi.stagetrack.dto.Dtos.OffreRequest;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.Offre.TypeStage;
import com.ensi.stagetrack.service.CandidatureService;
import com.ensi.stagetrack.service.OffreService;
import com.ensi.stagetrack.util.CsvUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

import java.util.List;

/**
 * Admin : tout le CRUD. Collaborateur (recruteur) : consulter et ajouter seulement (exigence du CDC).
 * Etudiant : consulter et postuler.
 */
@RestController
@RequestMapping("/api/offres")
@PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
public class OffreController {

    private final OffreService offreService;
    private final CandidatureService candidatureService;

    public OffreController(OffreService offreService, CandidatureService candidatureService) {
        this.offreService = offreService;
        this.candidatureService = candidatureService;
    }

    @GetMapping
    public PageResponse<OffreDto> list(@RequestParam(required = false) String search,
                                       @RequestParam(required = false) Long entrepriseId,
                                       @RequestParam(required = false) TypeStage type,
                                       @RequestParam(required = false) StatutOffre statut,
                                       @RequestParam(required = false) Integer dureeMax,
                                       @RequestParam(defaultValue = "false") boolean mine,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       Authentication authentication) {
        String createurEmail = mine ? authentication.getName() : null;
        return offreService.list(search, entrepriseId, type, statut, dureeMax, createurEmail,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "id")));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> export() {
        List<String[]> rows = offreService.listAll().stream().map(o -> new String[]{
                String.valueOf(o.id()), o.libelle(), o.entrepriseNom(), o.type().name(),
                o.duree() == null ? "" : o.duree().toString(),
                o.gratification() == null ? "" : o.gratification().toString(),
                o.dateLimite() == null ? "" : o.dateLimite().toString(),
                o.statut().name()}).toList();
        byte[] csv = CsvUtil.toCsv(new String[]{"id", "libelle", "entreprise", "type", "duree_mois",
                "gratification", "date_limite", "statut"}, rows);
        return CsvUtil.response("offres.csv", csv);
    }

    @GetMapping("/{id}")
    public OffreDto get(@PathVariable Long id) {
        return offreService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR')")
    public ResponseEntity<OffreDto> create(@Valid @RequestBody OffreRequest request,
                                           Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offreService.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OffreDto update(@PathVariable Long id, @Valid @RequestBody OffreRequest request) {
        return offreService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offreService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Postuler à une offre avec le compte connecté (Etudiant ou Admin). */
    @PostMapping("/{id}/candidatures")
    @PreAuthorize("hasAnyRole('ADMIN','ETUDIANT')")
    public ResponseEntity<CandidatureDto> postuler(@PathVariable Long id,
                                                   @Valid @RequestBody(required = false) CandidatureRequest request,
                                                   Authentication authentication) {
        String commentaire = request == null ? null : request.commentaire();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidatureService.postuler(id, authentication.getName(), commentaire));
    }
}
