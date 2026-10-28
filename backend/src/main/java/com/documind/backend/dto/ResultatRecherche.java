package com.documind.backend.dto;

import com.documind.backend.repository.ChunkTrouve;

public record ResultatRecherche(
        Long chunkId,
        Long documentId,
        String nomDocument,
        Integer numeroPage,
        String extrait,
        double score) {

    public static ResultatRecherche de(ChunkTrouve chunk) {
        return new ResultatRecherche(
                chunk.getId(),
                chunk.getDocumentId(),
                chunk.getNomDocument(),
                chunk.getNumeroPage(),
                chunk.getContenu(),
                chunk.getScore());
    }
}