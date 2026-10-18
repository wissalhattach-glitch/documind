package com.documind.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant dateCreation;

    @ManyToMany
    @JoinTable(
        name = "conversation_documents",
        joinColumns = @JoinColumn(name = "conversation_id"),
        inverseJoinColumns = @JoinColumn(name = "document_id")
    )
    @Builder.Default
    private Set<Document> documents = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = Instant.now();
        }
    }

    public void renommer(String nouveauTitre) {
        this.titre = nouveauTitre;
    }
}