package com.ensi.stagetrack.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Équivalent de l'entité Projet du CDC. */
@Entity
@Table(name = "offres")
public class Offre {

    public enum TypeStage {
        PFE, ETE
    }

    public enum StatutOffre {
        OUVERTE, FERMEE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    /** Équivalent du budget du CDC : gratification mensuelle. */
    private Double gratification;

    /** Durée en mois. */
    private Integer duree;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeStage type = TypeStage.PFE;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOffre statut = StatutOffre.OUVERTE;

    // Relation Many-to-One du CDC (idClient devient entreprise_id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entreprise_id", nullable = false)
    private Entreprise entreprise;

    /** Recruteur (ou admin) qui a publié l'offre. Peut être nul pour les offres de démonstration. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    private User createur;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Candidature> candidatures = new ArrayList<>();

    public Offre() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Double getGratification() {
        return gratification;
    }

    public void setGratification(Double gratification) {
        this.gratification = gratification;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeStage getType() {
        return type;
    }

    public void setType(TypeStage type) {
        this.type = type;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }

    public StatutOffre getStatut() {
        return statut;
    }

    public void setStatut(StatutOffre statut) {
        this.statut = statut;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public User getCreateur() {
        return createur;
    }

    public void setCreateur(User createur) {
        this.createur = createur;
    }

    public List<Candidature> getCandidatures() {
        return candidatures;
    }

    public void setCandidatures(List<Candidature> candidatures) {
        this.candidatures = candidatures;
    }
}
