package com.documind.backend.controller;

import com.documind.backend.dto.*;
import com.documind.backend.entity.User;
import com.documind.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.inscrire(request.nom(), request.email(), request.motDePasse());
        return new UserResponse(user.getId(), user.getNom(), user.getEmail());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return new AuthResponse(authService.connecter(request.email(), request.motDePasse()));
    }
}