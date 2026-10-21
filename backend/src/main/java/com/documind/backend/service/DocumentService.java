package com.documind.backend.service;

import com.documind.backend.entity.Document;
import com.documind.backend.exception.DocumentIntrouvableException;
import com.documind.backend.exception.FichierInvalideException;
import com.documind.backend.repository.DocumentRepository;
import com.documind.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Transactional
    public Document importer(MultipartFile fichier, Long userId) {
        if (fichier == null || fichier.isEmpty()) {
            throw new FichierInvalideException("Aucun fichier reçu");
        }
        if (!estUnPdf(fichier)) {
            throw new FichierInvalideException("Le fichier n'est pas un PDF valide");
        }

        String nomStocke = storageService.sauvegarder(fichier);

        try {
            Document document = Document.builder()
                    .user(userRepository.getReferenceById(userId))
                    .nomFichier(nettoyerNom(fichier.getOriginalFilename()))
                    .cheminStockage(nomStocke)
                    .tailleOctets(fichier.getSize())
                    .build();
            return documentRepository.save(document);
        } catch (RuntimeException e) {
            storageService.supprimer(nomStocke);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<Document> lister(Long userId, Pageable pageable) {
        return documentRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Document trouver(Long id, Long userId) {
        return documentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(DocumentIntrouvableException::new);
    }

    // un vrai PDF commence toujours par "%PDF-"
    private boolean estUnPdf(MultipartFile fichier) {
        try (InputStream in = fichier.getInputStream()) {
            byte[] debut = in.readNBytes(5);
            return debut.length == 5
                    && new String(debut, StandardCharsets.US_ASCII).equals("%PDF-");
        } catch (IOException e) {
            return false;
        }
    }

    // garde seulement le nom du fichier (sans dossier) et limite sa longueur
    private String nettoyerNom(String nom) {
        if (nom == null || nom.isBlank()) {
            return "document.pdf";
        }
        String propre = nom.replace('\\', '/');
        propre = propre.substring(propre.lastIndexOf('/') + 1).trim();
        if (propre.isEmpty()) {
            return "document.pdf";
        }
        return propre.length() > 255 ? propre.substring(propre.length() - 255) : propre;
    }
}