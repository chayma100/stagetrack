package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.PageResponse;
import com.ensi.stagetrack.dto.Dtos.UpdateProfileRequest;
import com.ensi.stagetrack.dto.Dtos.UserDto;
import com.ensi.stagetrack.exception.BadRequestException;
import com.ensi.stagetrack.exception.NotFoundException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Role;
import com.ensi.stagetrack.model.User;
import com.ensi.stagetrack.repository.RoleRepository;
import com.ensi.stagetrack.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> list(Pageable pageable) {
        return PageResponse.of(userRepository.findAll(pageable).map(Mapper::toUserDto));
    }

    @Transactional(readOnly = true)
    public UserDto me(String email) {
        return Mapper.toUserDto(findByEmail(email));
    }

    public UserDto updateMe(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        user.setTel(request.tel());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }
        return Mapper.toUserDto(userRepository.save(user));
    }

    /** Remplace les rôles d'un utilisateur. Les noms sont donnés sans préfixe : ADMIN, COLLABORATEUR, ETUDIANT. */
    public UserDto updateRoles(Long id, Set<String> roleNames) {
        if (roleNames.isEmpty()) {
            throw new BadRequestException("Un utilisateur doit avoir au moins un rôle");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable : " + id));
        Set<Role> roles = new HashSet<>();
        for (String name : roleNames) {
            String libelle = name.startsWith("ROLE_") ? name : "ROLE_" + name;
            roles.add(roleRepository.findByLibelle(libelle)
                    .orElseThrow(() -> new BadRequestException("Rôle inconnu : " + name)));
        }
        user.setRoles(roles);
        return Mapper.toUserDto(userRepository.save(user));
    }

    public void delete(Long id, String currentEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable : " + id));
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new BadRequestException("Vous ne pouvez pas supprimer votre propre compte");
        }
        userRepository.delete(user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }
}
