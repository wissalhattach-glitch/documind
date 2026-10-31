package com.documind.backend.repository;

import com.documind.backend.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    long countByDocumentId(Long documentId);

    @Modifying
    @Query("delete from DocumentChunk c where c.document.id = :documentId")
    void deleteAllByDocumentId(@Param("documentId") Long documentId);

    // les chunks les plus proches de la question, dans tous les documents de l'utilisateur
    @Query(value = """
            SELECT c.id AS "id",
                   c.document_id AS "documentId",
                   d.nom_fichier AS "nomDocument",
                   c.numero_page AS "numeroPage",
                   c.contenu AS "contenu",
                   1 - (c.embedding <=> CAST(:vecteur AS vector)) AS "score"
            FROM document_chunks c
            JOIN documents d ON d.id = c.document_id
            WHERE c.user_id = :userId
              AND d.statut = 'READY'
            ORDER BY c.embedding <=> CAST(:vecteur AS vector)
            LIMIT :limite
            """, nativeQuery = true)
    List<ChunkTrouve> rechercherProches(@Param("vecteur") String vecteur,
                                        @Param("userId") Long userId,
                                        @Param("limite") int limite);

    // pareil, mais seulement dans les documents choisis pour la conversation
    @Query(value = """
            SELECT c.id AS "id",
                   c.document_id AS "documentId",
                   d.nom_fichier AS "nomDocument",
                   c.numero_page AS "numeroPage",
                   c.contenu AS "contenu",
                   1 - (c.embedding <=> CAST(:vecteur AS vector)) AS "score"
            FROM document_chunks c
            JOIN documents d ON d.id = c.document_id
            WHERE c.user_id = :userId
              AND d.statut = 'READY'
              AND c.document_id IN (:documentIds)
            ORDER BY c.embedding <=> CAST(:vecteur AS vector)
            LIMIT :limite
            """, nativeQuery = true)
    List<ChunkTrouve> rechercherProchesDansDocuments(@Param("vecteur") String vecteur,
                                                     @Param("userId") Long userId,
                                                     @Param("documentIds") Collection<Long> documentIds,
                                                     @Param("limite") int limite);
}