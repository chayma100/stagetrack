package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.OffreDto;
import com.ensi.stagetrack.dto.Dtos.OffreRequest;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.exception.NotFoundException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Entreprise;
import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.Offre.TypeStage;
import com.ensi.stagetrack.repository.EntrepriseRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import com.ensi.stagetrack.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OffreService {

    private final OffreRepository offreRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final UserRepository userRepository;

    public OffreService(OffreRepository offreRepository, EntrepriseRepository entrepriseRepository,
                        UserRepository userRepository) {
        this.offreRepository = offreRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<OffreDto> list(String search, Long entrepriseId, TypeStage type,
                                       StatutOffre statut, Integer dureeMax, String createurEmail,
                                       Pageable pageable) {
        Specification<Offre> spec = buildSpec(search, entrepriseId, type, statut, dureeMax, createurEmail);
        return PageResponse.of(offreRepository.findAll(spec, pageable).map(Mapper::toOffreDto));
    }

    /** Toutes les offres (sans pagination), pour l'export CSV. */
    @Transactional(readOnly = true)
    public List<OffreDto> listAll() {
        return offreRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(Mapper::toOffreDto).toList();
    }

    @Transactional(readOnly = true)
    public OffreDto get(Long id) {
        return Mapper.toOffreDto(find(id));
    }

    public OffreDto create(OffreRequest request, String createurEmail) {
        Offre o = new Offre();
        apply(o, request);
        o.setCreateur(userRepository.findByEmail(createurEmail).orElse(null));
        return Mapper.toOffreDto(offreRepository.save(o));
    }

    public OffreDto update(Long id, OffreRequest request) {
        Offre o = find(id);
        apply(o, request);
        return Mapper.toOffreDto(offreRepository.save(o));
    }

    public void delete(Long id) {
        offreRepository.delete(find(id));
    }

    private Offre find(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + id));
    }

    private void apply(Offre o, OffreRequest r) {
        Entreprise entreprise = entrepriseRepository.findById(r.entrepriseId())
                .orElseThrow(() -> new NotFoundException("Entreprise introuvable : " + r.entrepriseId()));
        o.setLibelle(r.libelle().trim());
        o.setGratification(r.gratification());
        o.setDuree(r.duree());
        o.setDescription(r.description());
        o.setType(r.type());
        o.setDateLimite(r.dateLimite());
        o.setStatut(r.statut() != null ? r.statut() : StatutOffre.OUVERTE);
        o.setEntreprise(entreprise);
    }

    private Specification<Offre> buildSpec(String search, Long entrepriseId, TypeStage type,
                                           StatutOffre statut, Integer dureeMax, String createurEmail) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                Join<Offre, Entreprise> entreprise = root.join("entreprise");
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("libelle")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(entreprise.get("nom")), like)));
            }
            if (entrepriseId != null) {
                predicates.add(cb.equal(root.get("entreprise").get("id"), entrepriseId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (statut != null) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }
            if (createurEmail != null) {
                predicates.add(cb.equal(root.get("createur").get("email"), createurEmail));
            }
            if (dureeMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("duree"), dureeMax));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
