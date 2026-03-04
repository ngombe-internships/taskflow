package com.duva.taskflow.service;

import com.duva.taskflow.dto.AuthResponse;
import com.duva.taskflow.dto.LoginRequest;
import com.duva.taskflow.dto.RegisterRequest;
import com.duva.taskflow.entity.RefreshToken;
import com.duva.taskflow.entity.Role;
import com.duva.taskflow.entity.User;
import com.duva.taskflow.exception.UserAlreadyExistsException;
import com.duva.taskflow.exception.RoleNotFoundException;
import com.duva.taskflow.exception.InvalidRefreshTokenException;
import com.duva.taskflow.repository.RefreshTokenRepository;
import com.duva.taskflow.repository.RoleRepository;
import com.duva.taskflow.repository.UserRepository;
import com.duva.taskflow.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // REGISTER

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER not found in database"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRole(userRole);

        userRepository.save(user);

        log.info("User registered: {}", request.getEmail());

        //  Passe le role au token
        String accessToken = jwtService.generateAccessToken(user.getEmail(), userRole.getName());
        String refreshToken = createAndStoreRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    // LOGIN

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User logged in: {}", request.getEmail());

        //  Passe le role au token
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().getName());
        String refreshToken = createAndStoreRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    // REFRESH (ROTATION)

    @Transactional
    public AuthResponse refresh(String requestRefreshToken) {

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(requestRefreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (storedToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        // ROTATION - Révoquer l'ancien token
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();

        log.info("Refresh token used for: {}", user.getEmail());

        //  Passe le role au nouveau token
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().getName());
        String newRefreshToken = createAndStoreRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    // LOGOUT

    @Transactional
    public void logout(String refreshToken) {

        RefreshToken storedToken = refreshTokenRepository
                .findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Token not found"));

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.info("User logged out");
    }

    // PRIVATE METHOD

    private String createAndStoreRefreshToken(User user) {

        String token = jwtService.generateRefreshToken(user.getEmail());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setRevoked(false);
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);

        return token;
    }
}