package com.documind.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "source_references")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    // peut être vide si le document a été supprimé (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id")
    private DocumentChunk chunk;

    @Column(name = "nom_document", nullable = false, length = 255)
    private String nomDocument;

    @Column(name = "numero_page", nullable = false)
    private int numeroPage;

    @Column(nullable = false, columnDefinition = "text")
    private String extrait;

    @Column(name = "score_similarite", nullable = false)
    private double scoreSimilarite;
}