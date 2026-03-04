package com.duva.taskflow.controller;

import com.duva.taskflow.dto.AuthResponse;
import com.duva.taskflow.dto.LoginRequest;
import com.duva.taskflow.dto.RefreshRequest;
import com.duva.taskflow.dto.RegisterRequest;
import com.duva.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    //  FIX: Type de retour doit être AuthResponse, pas String
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    //  FIX: Appel la bonne méthode refresh() et passe le token
    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
    }

    @GetMapping("/test")
    public String test() {
        return "CONTROLLER WORKING";
    }
}