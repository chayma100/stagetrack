package com.ensi.stagetrack.mapper;

import com.ensi.stagetrack.dto.Dtos.CandidatureDto;
import com.ensi.stagetrack.dto.Dtos.EntrepriseDto;
import com.ensi.stagetrack.dto.Dtos.OffreDto;
import com.ensi.stagetrack.dto.Dtos.UserDto;
import com.ensi.stagetrack.model.Candidature;
import com.ensi.stagetrack.model.Entreprise;
import com.ensi.stagetrack.model.Offre;
import com.ensi.stagetrack.model.User;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Conversions entité vers DTO. À appeler dans une transaction (les relations sont chargées à la demande). */
public final class Mapper {

    private Mapper() {
    }

    public static UserDto toUserDto(User u) {
        Set<String> roles = u.getRoles().stream()
                .map(r -> r.getLibelle().replace("ROLE_", ""))
                .collect(Collectors.toCollection(TreeSet::new));
        return new UserDto(u.getId(), u.getEmail(), u.getTel(), u.getNom(), u.getPrenom(), roles);
    }

    public static EntrepriseDto toEntrepriseDto(Entreprise e, long nbOffres) {
        return new EntrepriseDto(e.getId(), e.getNom(), e.getTel(), e.getEmail(), e.getAdresse(),
                e.getSecteur(), e.getSiteWeb(), nbOffres);
    }

    public static OffreDto toOffreDto(Offre o) {
        return new OffreDto(o.getId(), o.getLibelle(), o.getGratification(), o.getDuree(),
                o.getDescription(), o.getType(), o.getDateLimite(), o.getStatut(),
                o.getEntreprise().getId(), o.getEntreprise().getNom());
    }

    public static CandidatureDto toCandidatureDto(Candidature c) {
        User u = c.getUser();
        Offre o = c.getOffre();
        return new CandidatureDto(c.getId(), c.getStatut(), c.getDateEnvoi(), c.getCommentaire(),
                u.getId(), u.getPrenom() + " " + u.getNom(), u.getEmail(),
                o.getId(), o.getLibelle(), o.getEntreprise().getNom());
    }
}
