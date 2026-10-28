package com.documind.backend.service;

import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentChunk;
import com.documind.backend.repository.DocumentChunkRepository;
import com.documind.backend.repository.DocumentRepository;
import com.documind.backend.exception.FichierInvalideException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private static final int TAILLE_LOT = 16;     // chunks envoyés à Ollama en une fois
    private static final int MAX_MOTS = 500_000;  // limite décidée dans le cahier des charges

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final StorageService storageService;
    private final PdfExtractor pdfExtractor;
    private final ChunkingService chunkingService;
    private final EmbeddingProvider embeddingProvider;
    private final TransactionTemplate transaction;

    public Document traiter(Long documentId, Long userId) {
        // 404 si le document n'existe pas ou n'est pas à cet utilisateur
        Document document = documentService.trouver(documentId, userId);

        document.marquerEnCours();
        documentRepository.save(document);

        try {
            List<String> pages = pdfExtractor.extraireParPage(
                    storageService.resoudre(document.getCheminStockage()));
            verifierTaille(pages);

            List<Chunk> chunks = chunkingService.decouper(pages);
            if (chunks.isEmpty()) {
                throw new FichierInvalideException("Aucun texte trouvé dans le PDF (PDF scanné ?)");
            }

            List<DocumentChunk> entites = vectoriser(document, userId, chunks);

            // une seule transaction : on remplace les anciens chunks par les nouveaux
            transaction.executeWithoutResult(status -> {
                chunkRepository.deleteAllByDocumentId(documentId);
                chunkRepository.saveAll(entites);
            });

            document.marquerPret();
        } catch (Exception e) {
            log.error("Échec du traitement du document {}", documentId, e);
            document.marquerEchec(messagePour(e));
        }

        return documentRepository.save(document);
    }

    private List<DocumentChunk> vectoriser(Document document, Long userId, List<Chunk> chunks) {
        List<DocumentChunk> resultat = new ArrayList<>(chunks.size());

        for (int debut = 0; debut < chunks.size(); debut += TAILLE_LOT) {
            List<Chunk> lot = chunks.subList(debut, Math.min(debut + TAILLE_LOT, chunks.size()));
            List<float[]> vecteurs = embeddingProvider.embedderDocuments(
                    lot.stream().map(Chunk::contenu).toList());

            for (int i = 0; i < lot.size(); i++) {
                Chunk chunk = lot.get(i);
                resultat.add(DocumentChunk.builder()
                        .document(document)
                        .userId(userId)
                        .contenu(chunk.contenu())
                        .numeroPage(chunk.numeroPage())
                        .position(chunk.position())
                        .embedding(vecteurs.get(i))
                        .modeleEmbedding(embeddingProvider.nomModele())
                        .build());
            }
        }
        return resultat;
    }

    private void verifierTaille(List<String> pages) {
        long mots = 0;
        for (String page : pages) {
            String texte = page.strip();
            if (!texte.isEmpty()) {
                mots += texte.split("\\s+").length;
            }
        }
        if (mots > MAX_MOTS) {
            throw new FichierInvalideException("Le texte du PDF dépasse " + MAX_MOTS + " mots");
        }
    }

    // message lisible pour l'utilisateur (sans détails techniques)
    private String messagePour(Exception e) {
        if (e instanceof FichierInvalideException) {
            return e.getMessage();
        }
        return "Erreur pendant l'analyse du document";
    }
}