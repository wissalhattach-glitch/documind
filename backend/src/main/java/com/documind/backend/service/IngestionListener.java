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

    // lancé APRÈS le commit de l'import (le document existe bien en base), dans un autre thread
    @Async("ingestionExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void surImport(DocumentImporteEvent event) {
        try {
            ingestionService.traiter(event.documentId(), event.userId());
        } catch (Exception e) {
            log.error("Analyse impossible pour le document {}", event.documentId(), e);
        }
    }
}