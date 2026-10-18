package com.documind.backend.repository;

import com.documind.backend.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    long countByDocumentId(Long documentId);

    @Modifying
    @Query("delete from DocumentChunk c where c.document.id = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);
}