package com.ensi.stagetrack.repository;

import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface OffreRepository extends JpaRepository<Offre, Long>, JpaSpecificationExecutor<Offre> {

    long countByEntrepriseId(Long entrepriseId);

    long countByStatut(StatutOffre statut);

    List<Offre> findByEntrepriseIdOrderByIdDesc(Long entrepriseId);

    List<Offre> findTop5ByStatutAndDateLimiteBetweenOrderByDateLimiteAsc(
            StatutOffre statut, LocalDate debut, LocalDate fin);

    /** Retourne des paires (nom de l'entreprise, nombre d'offres), triées par nombre décroissant. */
    @Query("select o.entreprise.nom, count(o) from Offre o "
            + "group by o.entreprise.nom order by count(o) desc")
    List<Object[]> topEntreprises(Pageable pageable);
}
