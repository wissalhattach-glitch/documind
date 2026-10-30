package com.documind.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank(message = "La question est obligatoire")
        @Size(max = 1000, message = "La question est trop longue (1000 caractères max)")
        String question) {
}