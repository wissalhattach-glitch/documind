package com.documind.backend.dto;

import com.documind.backend.entity.Conversation;

import java.time.Instant;

public record ConversationResume(Long id, String titre, Instant dateCreation) {

    public static ConversationResume de(Conversation conversation) {
        return new ConversationResume(conversation.getId(), conversation.getTitre(),
                conversation.getDateCreation());
    }
}