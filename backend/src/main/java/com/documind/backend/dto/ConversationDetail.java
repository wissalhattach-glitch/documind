package com.documind.backend.dto;

import java.time.Instant;
import java.util.List;

public record ConversationDetail(
        Long id,
        String titre,
        Instant dateCreation,
        List<DocumentResume> documents,
        List<MessageResponse> messages) {
}