package com.documind.backend.dto;

import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentStatus;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        String nomFichier,
        long tailleOctets,
        Integer nombrePages,
        DocumentStatus statut,
        String messageErreur,
        Instant dateUpload
) {
    public static DocumentResponse de(Document d) {
        return new DocumentResponse(
                d.getId(),
                d.getNomFichier(),
                d.getTailleOctets(),
                d.getNombrePages(),
                d.getStatut(),
                d.getMessageErreur(),
                d.getDateUpload()
        );
    }
}