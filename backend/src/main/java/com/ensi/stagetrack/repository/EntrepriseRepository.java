package com.ensi.stagetrack.repository;

import com.ensi.stagetrack.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EntrepriseRepository
        extends JpaRepository<Entreprise, Long>, JpaSpecificationExecutor<Entreprise> {

    /** Recherche une entreprise par son nom, sans tenir compte des majuscules. */
    Optional<Entreprise> findFirstByNomIgnoreCase(String nom);
}
