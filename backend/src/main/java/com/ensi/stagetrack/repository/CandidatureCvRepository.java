package com.ensi.stagetrack.repository;

import com.ensi.stagetrack.model.CandidatureCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidatureCvRepository extends JpaRepository<CandidatureCv, Long> {

    Optional<CandidatureCv> findByCandidatureId(Long candidatureId);
}
