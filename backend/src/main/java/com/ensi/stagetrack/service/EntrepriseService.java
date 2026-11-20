package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.EntrepriseDetailDto;
import com.ensi.stagetrack.dto.Dtos.EntrepriseDto;
import com.ensi.stagetrack.dto.Dtos.EntrepriseRequest;
import com.ensi.stagetrack.dto.Dtos.OffreDto;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.exception.NotFoundException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Entreprise;
import com.ensi.stagetrack.repository.EntrepriseRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EntrepriseService {

    private final EntrepriseRepository entrepriseRepository;
    private final OffreRepository offreRepository;

    public EntrepriseService(EntrepriseRepository entrepriseRepository, OffreRepository offreRepository) {
        this.entrepriseRepository = entrepriseRepository;
        this.offreRepository = offreRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EntrepriseDto> list(String q, Pageable pageable) {
        Specification<Entreprise> spec = (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("nom")), like),
                    cb.like(cb.lower(root.get("secteur")), like),
                    cb.like(cb.lower(root.get("adresse")), like));
        };
        Page<Entreprise> page = entrepriseRepository.findAll(spec, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    @Transactional(readOnly = true)
    public EntrepriseDetailDto get(Long id) {
        Entreprise e = find(id);
        List<OffreDto> offres = offreRepository.findByEntrepriseIdOrderByIdDesc(id).stream()
                .map(Mapper::toOffreDto).toList();
        return new EntrepriseDetailDto(toDto(e), offres);
    }

    public EntrepriseDto create(EntrepriseRequest request) {
        Entreprise e = new Entreprise();
        apply(e, request);
        return toDto(entrepriseRepository.save(e));
    }

    public EntrepriseDto update(Long id, EntrepriseRequest request) {
        Entreprise e = find(id);
        apply(e, request);
        return toDto(entrepriseRepository.save(e));
    }

    public void delete(Long id) {
        Entreprise e = find(id);
        entrepriseRepository.delete(e);
    }

    private Entreprise find(Long id) {
        return entrepriseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable : " + id));
    }

    private void apply(Entreprise e, EntrepriseRequest r) {
        e.setNom(r.nom().trim());
        e.setTel(r.tel());
        e.setEmail(r.email());
        e.setAdresse(r.adresse());
        e.setSecteur(r.secteur());
        e.setSiteWeb(r.siteWeb());
    }

    private EntrepriseDto toDto(Entreprise e) {
        return Mapper.toEntrepriseDto(e, offreRepository.countByEntrepriseId(e.getId()));
    }
}
