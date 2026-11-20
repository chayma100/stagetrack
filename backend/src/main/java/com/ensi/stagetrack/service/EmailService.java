package com.ensi.stagetrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String adminEmail;
    private final String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.admin.email}") String adminEmail,
                        @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
        this.from = from;
    }

    /** Exigence 5 du CDC : après une inscription, un email est envoyé à l'admin. */
    @Async
    public void notifyAdminNewUser(String nom, String prenom, String email) {
        send(adminEmail,
                "StageTrack : nouvelle inscription",
                "Un nouvel utilisateur vient de s'inscrire sur StageTrack.\n\n"
                        + "Nom : " + nom + "\n"
                        + "Prénom : " + prenom + "\n"
                        + "Email : " + email + "\n");
    }

    /** Prévient l'étudiant que le statut de sa candidature a changé. */
    @Async
    public void notifyCandidatureStatut(String toEmail, String offre, String entreprise, String statut) {
        send(toEmail,
                "StageTrack : mise à jour de votre candidature",
                "Le statut de votre candidature à l'offre « " + offre + " » (" + entreprise
                        + ") est maintenant : " + statut + ".\n");
    }

    private void send(String to, String subject, String text) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("Aucun serveur d'email configuré : message '{}' non envoyé à {}", subject, to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            sender.send(message);
            log.info("Email '{}' envoyé à {}", subject, to);
        } catch (Exception e) {
            // Une panne SMTP ne doit jamais faire échouer l'opération métier
            log.error("Échec de l'envoi de l'email '{}' à {} : {}", subject, to, e.getMessage());
        }
    }
}
