package com.documind.backend.repository;

import com.documind.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByDateCreationAsc(Long conversationId);

    // derniers messages, pour donner le contexte récent à l'IA
    List<Message> findTop10ByConversationIdOrderByDateCreationDesc(Long conversationId);
}