package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.CandidatureDto;
import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.exception.BadRequestException;
import com.ensi.stagetrack.exception.ConflictException;
import com.ensi.stagetrack.exception.NotFoundException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Candidature;
import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import com.ensi.stagetrack.model.CandidatureCv;
import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.User;
import com.ensi.stagetrack.repository.CandidatureCvRepository;
import com.ensi.stagetrack.repository.CandidatureRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import com.ensi.stagetrack.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.List;

@Service
@Transactional
public class CandidatureService {

    /** Taille maximale d'un CV : 5 Mo. */
    public static final long CV_TAILLE_MAX = 5L * 1024 * 1024;

    private final CandidatureRepository candidatureRepository;
    private final CandidatureCvRepository cvRepository;
    private final OffreRepository offreRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CandidatureService(CandidatureRepository candidatureRepository, CandidatureCvRepository cvRepository,
                              OffreRepository offreRepository,
                              UserRepository userRepository, EmailService emailService) {
        this.candidatureRepository = candidatureRepository;
        this.cvRepository = cvRepository;
        this.offreRepository = offreRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public CandidatureDto postuler(Long offreId, String userEmail, String commentaire) {
        return postuler(offreId, userEmail, commentaire, null);
    }

    /** Postule à une offre, avec un CV joint facultatif (PDF, DOC ou DOCX, 5 Mo maximum). */
    public CandidatureDto postuler(Long offreId, String userEmail, String commentaire, MultipartFile cv) {
        if (commentaire != null && commentaire.length() > 1000) {
            throw new BadRequestException("Le commentaire ne doit pas dépasser 1000 caractères");
        }
        boolean avecCv = cv != null && !cv.isEmpty();
        String cvNom = null;
        byte[] cvContenu = null;
        String cvType = null;
        if (avecCv) {
            cvNom = nomSur(cv.getOriginalFilename());
            cvType = typeAutorise(cvNom);
            if (cv.getSize() > CV_TAILLE_MAX) {
                throw new BadRequestException("Le CV ne doit pas dépasser 5 Mo");
            }
            try {
                cvContenu = cv.getBytes();
            } catch (IOException e) {
                throw new BadRequestException("Lecture du CV impossible");
            }
            verifierContenu(cvNom, cvContenu);
        }
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
        if (avecCv) {
            c.setCvNom(cvNom);
        }
        Candidature saved = candidatureRepository.save(c);
        if (avecCv) {
            CandidatureCv fichier = new CandidatureCv();
            fichier.setCandidature(saved);
            fichier.setTypeContenu(cvType);
            fichier.setContenu(cvContenu);
            cvRepository.save(fichier);
        }
        return Mapper.toCandidatureDto(saved);
    }

    /** Contenu d'un CV, pour le téléchargement. */
    public record CvTelechargement(String nom, String type, byte[] contenu) {
    }

    /** Télécharge le CV : admin, étudiant auteur de la candidature ou recruteur propriétaire de l'offre. */
    @Transactional(readOnly = true)
    public CvTelechargement cv(Long candidatureId, String email, boolean admin) {
        Candidature c = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new NotFoundException("Candidature introuvable : " + candidatureId));
        boolean auteur = c.getUser().getEmail().equalsIgnoreCase(email);
        boolean proprietaire = c.getOffre().getCreateur() != null
                && c.getOffre().getCreateur().getEmail().equalsIgnoreCase(email);
        if (!admin && !auteur && !proprietaire) {
            throw new AccessDeniedException("Accès refusé à ce CV");
        }
        CandidatureCv fichier = cvRepository.findByCandidatureId(candidatureId)
                .orElseThrow(() -> new NotFoundException("Aucun CV joint à cette candidature"));
        return new CvTelechargement(c.getCvNom() == null ? "cv" : c.getCvNom(),
                fichier.getTypeContenu(), fichier.getContenu());
    }

    private static String nomSur(String original) {
        String nom = original == null ? "" : original.replace('\\', '/');
        nom = nom.substring(nom.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}\"<>|:*?]", "_").trim();
        if (nom.isEmpty()) {
            nom = "cv";
        }
        return nom.length() > 200 ? nom.substring(nom.length() - 200) : nom;
    }

    private static String typeAutorise(String nom) {
        String minuscule = nom.toLowerCase(Locale.ROOT);
        if (minuscule.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (minuscule.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        if (minuscule.endsWith(".doc")) {
            return "application/msword";
        }
        throw new BadRequestException("Le CV doit être un fichier PDF, DOC ou DOCX");
    }

    /** Vérifie les premiers octets, pour refuser un fichier dont l'extension a été changée. */
    private static void verifierContenu(String nom, byte[] contenu) {
        String minuscule = nom.toLowerCase(Locale.ROOT);
        boolean ok;
        if (minuscule.endsWith(".pdf")) {
            ok = commence(contenu, 0x25, 0x50, 0x44, 0x46);
        } else if (minuscule.endsWith(".docx")) {
            ok = commence(contenu, 0x50, 0x4B);
        } else {
            ok = commence(contenu, 0xD0, 0xCF, 0x11, 0xE0);
        }
        if (!ok) {
            throw new BadRequestException("Le contenu du fichier ne correspond pas à son format");
        }
    }

    private static boolean commence(byte[] contenu, int... octets) {
        if (contenu.length < octets.length) {
            return false;
        }
        for (int i = 0; i < octets.length; i++) {
            if ((contenu[i] & 0xFF) != octets[i]) {
                return false;
            }
        }
        return true;
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
