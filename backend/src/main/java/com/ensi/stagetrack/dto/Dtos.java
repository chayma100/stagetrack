package com.ensi.stagetrack.dto;

import com.ensi.stagetrack.model.Candidature.StatutCandidature;
import com.ensi.stagetrack.model.Offre.StatutOffre;
import com.ensi.stagetrack.model.Offre.TypeStage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tous les objets échangés avec le front. L'entité User n'est jamais renvoyée telle quelle
 * afin de ne pas exposer le mot de passe.
 */
public final class Dtos {

    private Dtos() {
    }

    // ---------- Authentification et utilisateurs ----------

    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, message = "6 caractères minimum") String password,
            String tel,
            @NotBlank String nom,
            @NotBlank String prenom,
            /** ETUDIANT (défaut) ou COLLABORATEUR (recruteur). ADMIN est refusé. */
            String profil) {
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {
    }

    public record UserDto(Long id, String email, String tel, String nom, String prenom, Set<String> roles) {
    }

    public record AuthResponse(String token, UserDto user) {
    }

    public record UpdateProfileRequest(
            @NotBlank String nom,
            @NotBlank String prenom,
            String tel,
            @Size(min = 6, message = "6 caractères minimum") String newPassword) {
    }

    public record RolesRequest(@NotNull Set<String> roles) {
    }

    // ---------- Entreprises ----------

    public record EntrepriseRequest(
            @NotBlank String nom,
            String tel,
            @Email String email,
            String adresse,
            String secteur,
            String siteWeb) {
    }

    public record EntrepriseDto(
            Long id, String nom, String tel, String email, String adresse,
            String secteur, String siteWeb, long nbOffres) {
    }

    public record EntrepriseDetailDto(EntrepriseDto entreprise, List<OffreDto> offres) {
    }

    // ---------- Offres ----------

    public record OffreRequest(
            @NotBlank String libelle,
            @PositiveOrZero Double gratification,
            @NotNull @Positive Integer duree,
            String description,
            @NotNull TypeStage type,
            LocalDate dateLimite,
            StatutOffre statut,
            @NotNull Long entrepriseId) {
    }

    public record OffreDto(
            Long id, String libelle, Double gratification, Integer duree, String description,
            TypeStage type, LocalDate dateLimite, StatutOffre statut,
            Long entrepriseId, String entrepriseNom) {
    }

    // ---------- Candidatures ----------

    public record CandidatureRequest(@Size(max = 1000) String commentaire) {
    }

    public record StatutRequest(@NotNull StatutCandidature statut) {
    }

    public record CandidatureDto(
            Long id, StatutCandidature statut, LocalDate dateEnvoi, String commentaire,
            Long userId, String userNom, String userEmail,
            Long offreId, String offreLibelle, String entrepriseNom, String cvNom) {
    }

    // ---------- Statistiques ----------

    public record TopEntreprise(String nom, long nbOffres) {
    }

    public record StatsDto(
            long offresOuvertes,
            long totalOffres,
            long totalEntreprises,
            long totalCandidatures,
            Map<String, Long> candidaturesParStatut,
            List<TopEntreprise> topEntreprises,
            List<OffreDto> offresExpirantBientot) {
    }

    // ---------- Divers ----------

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {
        public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages());
        }
    }

    public record ErrorResponse(String timestamp, int status, String error, String message,
                                Map<String, String> fields) {
    }
}
