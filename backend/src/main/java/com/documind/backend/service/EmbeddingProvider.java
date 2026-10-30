package com.documind.backend.service;

import java.util.List;

public interface EmbeddingProvider {

    // vecteurs des chunks d'un document (même ordre que la liste reçue)
    List<float[]> embedderDocuments(List<String> textes);

    // vecteur d'une question posée par l'utilisateur
    float[] embedderQuestion(String question);

    // nom du modèle, stocké dans document_chunks.modele_embedding
    String nomModele();
}