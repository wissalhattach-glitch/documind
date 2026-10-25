package com.documind.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    // copie volontaire de l'id du propriétaire, pour filtrer la recherche vectorielle
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "text")
    private String contenu;

    @Column(name = "numero_page", nullable = false)
    private int numeroPage;

    @Column(nullable = false)
    private int position;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024) // doit être égal à la dimension de ton modèle d'embeddings
    @Column(nullable = false)
    private float[] embedding;

    @Column(name = "modele_embedding", nullable = false, length = 100)
    private String modeleEmbedding;
}
