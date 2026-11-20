package com.ensi.stagetrack.repository;

import com.ensi.stagetrack.model.Candidature;
import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    boolean existsByUserIdAndOffreId(Long userId, Long offreId);

    List<Candidature> findByUserIdOrderByDateEnvoiDescIdDesc(Long userId);

    Page<Candidature> findByStatut(StatutCandidature statut, Pageable pageable);

    /** Candidatures reçues sur les offres publiées par un recruteur. */
    Page<Candidature> findByOffreCreateurEmail(String email, Pageable pageable);

    Page<Candidature> findByOffreCreateurEmailAndStatut(String email, StatutCandidature statut, Pageable pageable);

    /** Retourne des paires (statut, nombre de candidatures). */
    @Query("select c.statut, count(c) from Candidature c group by c.statut")
    List<Object[]> countParStatut();
}
