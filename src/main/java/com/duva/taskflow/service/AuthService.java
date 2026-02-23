package com.duva.taskflow.service;

import com.duva.taskflow.dto.LoginRequest;
import com.duva.taskflow.dto.RegisterRequest;
import com.duva.taskflow.entity.Role;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.repository.RoleRepository;
import com.duva.taskflow.repository.UserRepository;
import com.duva.taskflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Repository pour accéder aux utilisateurs en base
    private final UserRepository userRepository;

    // Repository pour accéder aux rôles (ROLE_USER, ROLE_ADMIN)
    private final RoleRepository roleRepository;

    // Encoder pour sécuriser les mots de passe (BCrypt)
    private final PasswordEncoder passwordEncoder;

    // Gestionnaire d'authentification Spring Security
    private final AuthenticationManager authenticationManager;

    // Gestionnaire d'authentification JWT
    private final JwtService jwtService;


    //  INSCRIPTION

    public String register(RegisterRequest request) {

        // Vérifie si l’email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Email already exists";
        }

        // Récupération du rôle par défaut
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Création du nouvel utilisateur
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encodage du mot de passe
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Compte activé directement (temporaire)
        user.setEnabled(true);

        // Attribution du rôle
        user.setRole(userRole);

        // Sauvegarde en base
        userRepository.save(user);

        return "User registered successfully";
    }



    //  LOGIN

    public String login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Generate JWT after successful authentication
        return jwtService.generateToken(request.getEmail());
    }
}
