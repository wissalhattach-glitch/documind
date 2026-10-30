package com.documind.backend.controller;

import com.documind.backend.dto.QuestionRequest;
import com.documind.backend.dto.ReponseRag;
import com.documind.backend.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final RagService ragService;

    @PostMapping
    public ReponseRag poser(@Valid @RequestBody QuestionRequest requete,
                            @AuthenticationPrincipal Long userId) {
        return ragService.repondre(requete.question(), userId);
    }
}