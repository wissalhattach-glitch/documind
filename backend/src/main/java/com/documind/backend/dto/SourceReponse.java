package com.documind.backend.dto;

public record SourceReponse(
        Long chunkId,
        Long documentId,
        String nomDocument,
        Integer numeroPage,
        String extrait,
        double score) {

    private static final int LONGUEUR_MAX = 300;

    public static SourceReponse de(ResultatRecherche resultat) {
        String texte = resultat.extrait().replaceAll("\\s+", " ").strip();
        if (texte.length() > LONGUEUR_MAX) {
            texte = texte.substring(0, LONGUEUR_MAX) + "…";
        }
        return new SourceReponse(
                resultat.chunkId(),      
                resultat.documentId(),
                resultat.nomDocument(),
                resultat.numeroPage(),
                texte,
                resultat.score());
    }
}