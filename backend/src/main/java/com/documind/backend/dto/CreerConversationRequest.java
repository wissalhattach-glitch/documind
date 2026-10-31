package com.documind.backend.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record CreerConversationRequest(
        @Size(max = 200, message = "Le titre est trop long (200 caractères max)")
        String titre,

        @Size(max = 20, message = "20 documents maximum par conversation")
        List<Long> documentIds) {
}