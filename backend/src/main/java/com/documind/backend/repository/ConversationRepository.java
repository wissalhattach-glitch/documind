package com.documind.backend.repository;

import com.documind.backend.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Page<Conversation> findByUserId(Long userId, Pageable pageable);

    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    // charge aussi les documents ciblés (sinon la liste LAZY n'est pas disponible)
    @EntityGraph(attributePaths = "documents")
    Optional<Conversation> findWithDocumentsByIdAndUserId(Long id, Long userId);
}