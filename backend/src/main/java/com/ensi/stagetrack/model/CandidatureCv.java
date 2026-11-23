package com.ensi.stagetrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

/**
 * CV joint à une candidature. Le contenu est stocké en base (et non sur disque) pour survivre
 * aux redémarrages de Render. Il est dans une table à part pour ne pas être chargé avec les listes.
 */
@Entity
@Table(name = "candidature_cv")
public class CandidatureCv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidature_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Candidature candidature;

    @Column(name = "type_contenu", nullable = false, length = 100)
    private String typeContenu;

    @JdbcTypeCode(SqlTypes.LONG32VARBINARY)
    @Column(nullable = false)
    private byte[] contenu;

    public CandidatureCv() {
    }

    public Long getId() {
        return id;
    }

    public Candidature getCandidature() {
        return candidature;
    }

    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }

    public String getTypeContenu() {
        return typeContenu;
    }

    public void setTypeContenu(String typeContenu) {
        this.typeContenu = typeContenu;
    }

    public byte[] getContenu() {
        return contenu;
    }

    public void setContenu(byte[] contenu) {
        this.contenu = contenu;
    }
}
