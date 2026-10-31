package com.documind.backend.service;

import com.documind.backend.dto.ResultatRecherche;
import com.documind.backend.repository.ChunkTrouve;
import com.documind.backend.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RechercheService {

    private static final int NOMBRE_RESULTATS = 5;

    private final EmbeddingProvider embeddingProvider;
    private final DocumentChunkRepository chunkRepository;

    // dans tous les documents de l'utilisateur
    @Transactional(readOnly = true)
    public List<ResultatRecherche> rechercher(String question, Long userId) {
        float[] vecteur = embeddingProvider.embedderQuestion(question.strip());
        return convertir(chunkRepository.rechercherProches(versTexte(vecteur), userId, NOMBRE_RESULTATS));
    }

    // seulement dans les documents indiqués
    @Transactional(readOnly = true)
    public List<ResultatRecherche> rechercher(String question, Long userId, Collection<Long> documentIds) {
        float[] vecteur = embeddingProvider.embedderQuestion(question.strip());
        return convertir(chunkRepository.rechercherProchesDansDocuments(
                versTexte(vecteur), userId, documentIds, NOMBRE_RESULTATS));
    }

    private List<ResultatRecherche> convertir(List<ChunkTrouve> chunks) {
        return chunks.stream().map(ResultatRecherche::de).toList();
    }

    // pgvector lit un vecteur sous la forme "[0.1,0.2,0.3]"
    private String versTexte(float[] vecteur) {
        StringBuilder texte = new StringBuilder("[");
        for (int i = 0; i < vecteur.length; i++) {
            if (i > 0) {
                texte.append(',');
            }
            texte.append(vecteur[i]);
        }
        return texte.append(']').toString();
    }
}