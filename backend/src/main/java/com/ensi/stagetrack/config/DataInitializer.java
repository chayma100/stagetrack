package com.ensi.stagetrack.config;

import com.ensi.stagetrack.model.Entreprise;
import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.Offre.TypeStage;
import com.ensi.stagetrack.model.Role;
import com.ensi.stagetrack.model.User;
import com.ensi.stagetrack.repository.EntrepriseRepository;
import com.ensi.stagetrack.repository.OffreRepository;
import com.ensi.stagetrack.repository.RoleRepository;
import com.ensi.stagetrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Au démarrage : crée les rôles (Admin et Collaborateur du CDC, plus Etudiant), le compte admin, et (si app.seed-demo=true)
 * quelques entreprises et offres de démonstration.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EntrepriseRepository entrepriseRepository;
    private final OffreRepository offreRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean seedDemo;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           EntrepriseRepository entrepriseRepository, OffreRepository offreRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword,
                           @Value("${app.seed-demo}") boolean seedDemo) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.entrepriseRepository = entrepriseRepository;
        this.offreRepository = offreRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
        this.seedDemo = seedDemo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = roleRepository.findByLibelle(Role.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.ADMIN)));
        roleRepository.findByLibelle(Role.COLLABORATEUR)
                .orElseGet(() -> roleRepository.save(new Role(Role.COLLABORATEUR)));
        roleRepository.findByLibelle(Role.ETUDIANT)
                .orElseGet(() -> roleRepository.save(new Role(Role.ETUDIANT)));

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setNom("Admin");
            admin.setPrenom("StageTrack");
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            log.info("Compte admin créé : {}", adminEmail);
        }

        if (seedDemo && entrepriseRepository.count() == 0) {
            seedDemoData();
        }
        if (seedDemo) {
            seedDemoUsers();
        }
    }

    /** Comptes de démonstration (profil dev seulement) : un recruteur et un étudiant. */
    private void seedDemoUsers() {
        User recruteur = demoUser("recruteur@stagetrack.local", "Recruteur123!", "Ben Salah", "Sami", Role.COLLABORATEUR);
        demoUser("etudiant@stagetrack.local", "Etudiant123!", "Trabelsi", "Lina", Role.ETUDIANT);
        // les offres de démonstration sans propriétaire sont rattachées au recruteur
        for (Offre o : offreRepository.findAll()) {
            if (o.getCreateur() == null) {
                o.setCreateur(recruteur);
                offreRepository.save(o);
            }
        }
    }

    private User demoUser(String email, String password, String nom, String prenom, String libelleRole) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setPassword(passwordEncoder.encode(password));
            u.setNom(nom);
            u.setPrenom(prenom);
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByLibelle(libelleRole).orElseThrow());
            u.setRoles(roles);
            log.info("Compte de démonstration créé : {}", email);
            return userRepository.save(u);
        });
    }

    private void seedDemoData() {
        Entreprise e1 = entreprise("Capgemini Engineering", "Conseil et ingénierie", "Tunis");
        Entreprise e2 = entreprise("Vermeg", "Logiciels financiers", "Tunis");
        Entreprise e3 = entreprise("Wevioo", "Services numériques", "Tunis");

        LocalDate today = LocalDate.now();
        offre(e1, "PFE Développeur Full Stack", TypeStage.PFE, 6, 900.0, today.plusDays(30),
                "Conception et développement d'une application web Spring Boot et Angular.");
        offre(e1, "Stage d'été DevOps", TypeStage.ETE, 2, 500.0, today.plusDays(10),
                "Mise en place d'un pipeline CI/CD.");
        offre(e2, "PFE Ingénieur Data", TypeStage.PFE, 6, 1000.0, today.plusDays(45),
                "Construction de pipelines de données et de tableaux de bord.");
        offre(e3, "PFE Test et Qualité Logicielle", TypeStage.PFE, 5, 800.0, today.plusDays(20),
                "Automatisation des tests d'une application métier.");
        offre(e3, "Stage d'été Développement mobile", TypeStage.ETE, 2, 450.0, today.minusDays(5),
                "Développement d'écrans pour une application mobile.");
        log.info("Données de démonstration créées (3 entreprises, 5 offres)");
    }

    private Entreprise entreprise(String nom, String secteur, String adresse) {
        Entreprise e = new Entreprise();
        e.setNom(nom);
        e.setSecteur(secteur);
        e.setAdresse(adresse);
        e.setEmail("contact@" + nom.toLowerCase().replaceAll("[^a-z0-9]", "") + ".example");
        e.setTel("71 000 000");
        return entrepriseRepository.save(e);
    }

    private void offre(Entreprise e, String libelle, TypeStage type, int duree, double gratification,
                       LocalDate dateLimite, String description) {
        Offre o = new Offre();
        o.setEntreprise(e);
        o.setLibelle(libelle);
        o.setType(type);
        o.setDuree(duree);
        o.setGratification(gratification);
        o.setDateLimite(dateLimite);
        o.setDescription(description);
        o.setStatut(dateLimite.isBefore(LocalDate.now()) ? StatutOffre.FERMEE : StatutOffre.OUVERTE);
        offreRepository.save(o);
    }
}
