package com.ensi.stagetrack.repository;

import com.ensi.stagetrack.model.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EntrepriseRepository
        extends JpaRepository<Entreprise, Long>, JpaSpecificationExecutor<Entreprise> {
}
