package com.documind.backend.controller;

import com.documind.backend.dto.UserResponse;
import com.documind.backend.entity.User;
import com.documind.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        User user = authService.trouverParId(userId);
        return new UserResponse(user.getId(), user.getNom(), user.getEmail());
    }
}