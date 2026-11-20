package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.CandidatureDto;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.exception.BadRequestException;
import com.ensi.stagetrack.exception.ConflictException;
import com.ensi.stagetrack.exception.NotFoundException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Candidature;
import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.User;
import com.ensi.stagetrack.repository.CandidatureRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import com.ensi.stagetrack.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CandidatureService(CandidatureRepository candidatureRepository, OffreRepository offreRepository,
                              UserRepository userRepository, EmailService emailService) {
        this.candidatureRepository = candidatureRepository;
        this.offreRepository = offreRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public CandidatureDto postuler(Long offreId, String userEmail, String commentaire) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new NotFoundException("Offre introuvable : " + offreId));

        if (offre.getStatut() == StatutOffre.FERMEE
                || (offre.getDateLimite() != null && offre.getDateLimite().isBefore(LocalDate.now()))) {
            throw new BadRequestException("Cette offre n'accepte plus de candidatures");
        }
        if (candidatureRepository.existsByUserIdAndOffreId(user.getId(), offreId)) {
            throw new ConflictException("Vous avez déjà postulé à cette offre");
        }

        Candidature c = new Candidature();
        c.setUser(user);
        c.setOffre(offre);
        c.setCommentaire(commentaire);
        c.setStatut(StatutCandidature.ENVOYEE);
        c.setDateEnvoi(LocalDate.now());
        return Mapper.toCandidatureDto(candidatureRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<CandidatureDto> mine(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        return candidatureRepository.findByUserIdOrderByDateEnvoiDescIdDesc(user.getId()).stream()
                .map(Mapper::toCandidatureDto).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CandidatureDto> list(StatutCandidature statut, Pageable pageable) {
        Page<Candidature> page = statut == null
                ? candidatureRepository.findAll(pageable)
                : candidatureRepository.findByStatut(statut, pageable);
        return PageResponse.of(page.map(Mapper::toCandidatureDto));
    }

    /** Candidatures reçues : toutes pour l'admin, celles de ses propres offres pour un recruteur. */
    @Transactional(readOnly = true)
    public PageResponse<CandidatureDto> recues(String email, boolean admin, StatutCandidature statut,
                                               Pageable pageable) {
        if (admin) {
            return list(statut, pageable);
        }
        Page<Candidature> page = statut == null
                ? candidatureRepository.findByOffreCreateurEmail(email, pageable)
                : candidatureRepository.findByOffreCreateurEmailAndStatut(email, statut, pageable);
        return PageResponse.of(page.map(Mapper::toCandidatureDto));
    }

    /** Toutes les candidatures (sans pagination), pour l'export CSV. */
    @Transactional(readOnly = true)
    public List<CandidatureDto> listAll() {
        return candidatureRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(Mapper::toCandidatureDto).toList();
    }

    public CandidatureDto updateStatut(Long id, StatutCandidature statut, String email, boolean admin) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable : " + id));
        if (!admin) {
            User createur = c.getOffre().getCreateur();
            if (createur == null || !createur.getEmail().equalsIgnoreCase(email)) {
                throw new AccessDeniedException("Cette candidature ne concerne pas l'une de vos offres");
            }
        }
        c.setStatut(statut);
        Candidature saved = candidatureRepository.save(c);
        emailService.notifyCandidatureStatut(saved.getUser().getEmail(), saved.getOffre().getLibelle(),
                saved.getOffre().getEntreprise().getNom(), statut.name());
        return Mapper.toCandidatureDto(saved);
    }
}
