package com.documind.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionListener {

    private final IngestionService ingestionService;

    // s'exécute dans le thread "ingestion-1", après la sauvegarde du document
    @Async("ingestionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surDocumentImporte(DocumentImporteEvent evenement) {
        try {
            ingestionService.traiter(evenement.documentId(), evenement.userId());
        } catch (Exception e) {
            log.error("Erreur inattendue pendant l'analyse du document {}", evenement.documentId(), e);
        }
    }
}