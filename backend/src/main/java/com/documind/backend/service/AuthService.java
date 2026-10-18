package com.documind.backend.service;

import com.documind.backend.entity.User;
import com.documind.backend.exception.EmailDejaUtiliseException;
import com.documind.backend.exception.IdentifiantsInvalidesException;
import com.documind.backend.repository.UserRepository;
import com.documind.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public User inscrire(String nom, String email, String motDePasse) {
        String emailNormalise = email.trim().toLowerCase();

        if (userRepository.existsByEmail(emailNormalise)) {
            throw new EmailDejaUtiliseException();
        }

        User user = User.builder()
                .nom(nom.trim())
                .email(emailNormalise)
                .motDePasseHash(passwordEncoder.encode(motDePasse))
                .build();

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String connecter(String email, String motDePasse) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .filter(u -> passwordEncoder.matches(motDePasse, u.getMotDePasseHash()))
                .orElseThrow(IdentifiantsInvalidesException::new);

        return jwtService.genererToken(user);
    }

    @Transactional(readOnly = true)
    public User trouverParId(Long id) {
        return userRepository.findById(id).orElseThrow(IdentifiantsInvalidesException::new);
    }
}