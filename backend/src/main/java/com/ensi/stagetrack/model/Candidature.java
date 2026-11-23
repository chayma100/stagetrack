package com.ensi.stagetrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

/** Entité ajoutée au CDC : candidature d'un utilisateur à une offre. */
@Entity
@Table(name = "candidatures",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "offre_id"}))
public class Candidature {

    public enum StatutCandidature {
        ENVOYEE, ENTRETIEN, ACCEPTEE, REFUSEE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut = StatutCandidature.ENVOYEE;

    @Column(name = "date_envoi", nullable = false)
    private LocalDate dateEnvoi = LocalDate.now();

    @Column(length = 1000)
    private String commentaire;

    /** Nom du CV joint (le contenu est dans CandidatureCv), null si aucun CV. */
    @Column(name = "cv_nom", length = 255)
    private String cvNom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    public Candidature() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatutCandidature getStatut() {
        return statut;
    }

    public void setStatut(StatutCandidature statut) {
        this.statut = statut;
    }

    public LocalDate getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDate dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getCvNom() {
        return cvNom;
    }

    public void setCvNom(String cvNom) {
        this.cvNom = cvNom;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }
}
