package com.ensi.stagetrack.controller;

import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.dto.Dtos.RolesRequest;
import com.ensi.stagetrack.dto.Dtos.UpdateProfileRequest;
import com.ensi.stagetrack.dto.Dtos.UserDto;
import com.ensi.stagetrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // ----- Profil de l'utilisateur connecté (tous les rôles) -----

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
    public UserDto me(Authentication authentication) {
        return service.me(authentication.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','COLLABORATEUR','ETUDIANT')")
    public UserDto updateMe(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return service.updateMe(authentication.getName(), request);
    }

    // ----- Gestion des utilisateurs (Admin uniquement) -----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserDto> list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        return service.list(PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.ASC, "id")));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto updateRoles(@PathVariable Long id, @Valid @RequestBody RolesRequest request) {
        return service.updateRoles(id, request.roles());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
