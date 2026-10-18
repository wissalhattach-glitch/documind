package com.documind.backend.repository;

import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUserId(Long userId, Pageable pageable);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<Document> findByIdInAndUserIdAndStatut(Collection<Long> ids, Long userId, DocumentStatus statut);
}