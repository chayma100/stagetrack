package com.ensi.stagetrack.service;

import com.ensi.stagetrack.dto.Dtos.AuthResponse;
import com.ensi.stagetrack.dto.Dtos.LoginRequest;
import com.ensi.stagetrack.dto.Dtos.RegisterRequest;
import com.ensi.stagetrack.exception.BadRequestException;
import com.ensi.stagetrack.exception.ConflictException;
import com.ensi.stagetrack.mapper.Mapper;
import com.ensi.stagetrack.model.Role;
import com.ensi.stagetrack.model.User;
import com.ensi.stagetrack.repository.RoleRepository;
import com.ensi.stagetrack.repository.UserRepository;
import com.ensi.stagetrack.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Un compte existe déjà avec cet email");
        }
        String profil = request.profil() == null || request.profil().isBlank()
                ? "ETUDIANT" : request.profil().trim().toUpperCase();
        String libelleRole;
        if (profil.equals("ETUDIANT")) {
            libelleRole = Role.ETUDIANT;
        } else if (profil.equals("COLLABORATEUR")) {
            libelleRole = Role.COLLABORATEUR;
        } else {
            throw new BadRequestException("Profil invalide : choisissez ETUDIANT ou COLLABORATEUR");
        }
        Role role = roleRepository.findByLibelle(libelleRole)
                .orElseThrow(() -> new IllegalStateException("Rôle " + libelleRole + " absent de la base"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setTel(request.tel());
        user.setNom(request.nom().trim());
        user.setPrenom(request.prenom().trim());
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        User saved = userRepository.save(user);

        emailService.notifyAdminNewUser(saved.getNom(), saved.getPrenom(), saved.getEmail());
        return new AuthResponse(jwtService.generateToken(saved), Mapper.toUserDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        // Lève BadCredentialsException (traduite en 401) si les identifiants sont faux
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable après authentification"));
        return new AuthResponse(jwtService.generateToken(user), Mapper.toUserDto(user));
    }
}
