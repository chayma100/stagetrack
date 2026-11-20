package com.ensi.stagetrack.controller;

import com.ensi.stagetrack.dto.Dtos.CandidatureDto;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.dto.Dtos.StatutRequest;
import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import com.ensi.stagetrack.service.CandidatureService;
import com.ensi.stagetrack.util.CsvUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
public class CandidatureController {

    private final CandidatureService service;

    public CandidatureController(CandidatureService service) {
        this.service = service;
    }

    /** Les candidatures de l'utilisateur connecté (Etudiant ou Admin). */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','ETUDIANT')")
    public List<CandidatureDto> mine(Authentication authentication) {
        return service.mine(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<CandidatureDto> list(@RequestParam(required = false) StatutCandidature statut,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return service.list(statut, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "id")));
    }

    /** Candidatures reçues : toutes pour l'admin, celles de ses offres pour un recruteur. */
    @GetMapping("/recues")
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR')")
    public PageResponse<CandidatureDto> recues(@RequestParam(required = false) StatutCandidature statut,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.recues(authentication.getName(), admin, statut,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                        Sort.by(Sort.Direction.DESC, "id")));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> export() {
        List<String[]> rows = service.listAll().stream().map(c -> new String[]{
                String.valueOf(c.id()), c.userNom(), c.userEmail(), c.offreLibelle(), c.entrepriseNom(),
                c.statut().name(), c.dateEnvoi().toString(), c.commentaire()}).toList();
        byte[] csv = CsvUtil.toCsv(new String[]{"id", "candidat", "email", "offre", "entreprise",
                "statut", "date_envoi", "commentaire"}, rows);
        return CsvUtil.response("candidatures.csv", csv);
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR')")
    public CandidatureDto updateStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request,
                                       Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.updateStatut(id, request.statut(), authentication.getName(), admin);
    }
}
