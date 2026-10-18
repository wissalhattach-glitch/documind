package com.documind.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "chemin_stockage", nullable = false, length = 500)
    private String cheminStockage;

    @Column(name = "taille_octets", nullable = false)
    private long tailleOctets;

    @Column(name = "nombre_pages")
    private Integer nombrePages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DocumentStatus statut = DocumentStatus.UPLOADED;

    @Column(name = "message_erreur", columnDefinition = "text")
    private String messageErreur;

    @Column(name = "date_upload", nullable = false, updatable = false)
    private Instant dateUpload;

    @PrePersist
    protected void onCreate() {
        if (dateUpload == null) {
            dateUpload = Instant.now();
        }
    }

    public void marquerEnCours() {
        this.statut = DocumentStatus.PROCESSING;
        this.messageErreur = null;
    }

    public void marquerPret() {
        this.statut = DocumentStatus.READY;
        this.messageErreur = null;
    }

    public void marquerEchec(String message) {
        this.statut = DocumentStatus.FAILED;
        this.messageErreur = message;
    }
}