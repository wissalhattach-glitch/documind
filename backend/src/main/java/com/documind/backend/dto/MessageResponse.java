package com.documind.backend.dto;

import com.documind.backend.entity.Role;

import java.time.Instant;
import java.util.List;

public record MessageResponse(
        Long id,
        Role role,
        String contenu,
        Instant dateCreation,
        List<SourceHistorique> sources) {
}