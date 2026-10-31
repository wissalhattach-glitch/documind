package com.documind.backend.repository;

import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUserId(Long userId, Pageable pageable);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    List<Document> findByIdInAndUserIdAndStatut(Collection<Long> ids, Long userId, DocumentStatus statut);

    // change le statut de tous les documents qui ont l'un des statuts donnés
    @Modifying
    @Transactional
    @Query("update Document d set d.statut = :nouveau, d.messageErreur = :message where d.statut in :statuts")
    int changerStatut(@Param("statuts") List<DocumentStatus> statuts,
                      @Param("nouveau") DocumentStatus nouveau,
                      @Param("message") String message);
}