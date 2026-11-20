package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.OffreDto;
import com.ensi.stagetrack.dto.Dtos.StatsDto;
import com.ensi.stagetrack.dto.Dtos.TopEntreprise;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.repository.CandidatureRepository;
import com.ensi.stagetrack.repository.EntrepriseRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatsService {

    private final EntrepriseRepository entrepriseRepository;
    private final OffreRepository offreRepository;
    private final CandidatureRepository candidatureRepository;

    public StatsService(EntrepriseRepository entrepriseRepository, OffreRepository offreRepository,
                        CandidatureRepository candidatureRepository) {
        this.entrepriseRepository = entrepriseRepository;
        this.offreRepository = offreRepository;
        this.candidatureRepository = candidatureRepository;
    }

    public StatsDto compute() {
        // Tous les statuts apparaissent dans le résultat, même à zéro
        Map<String, Long> parStatut = new LinkedHashMap<>();
        for (StatutCandidature s : StatutCandidature.values()) {
            parStatut.put(s.name(), 0L);
        }
        for (Object[] row : candidatureRepository.countParStatut()) {
            parStatut.put(((StatutCandidature) row[0]).name(), (Long) row[1]);
        }

        List<TopEntreprise> top = offreRepository.topEntreprises(PageRequest.of(0, 5)).stream()
                .map(row -> new TopEntreprise((String) row[0], (Long) row[1]))
                .toList();

        LocalDate today = LocalDate.now();
        List<OffreDto> expirantBientot = offreRepository
                .findTop5ByStatutAndDateLimiteBetweenOrderByDateLimiteAsc(
                        StatutOffre.OUVERTE, today, today.plusDays(14))
                .stream().map(Mapper::toOffreDto).toList();

        return new StatsDto(
                offreRepository.countByStatut(StatutOffre.OUVERTE),
                offreRepository.count(),
                entrepriseRepository.count(),
                candidatureRepository.count(),
                parStatut,
                top,
                expirantBientot);
    }
}
