package com.documind.backend.service;

import com.documind.backend.entity.DocumentStatus;
import com.documind.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecuperationAuDemarrage {

    private final DocumentRepository documentRepository;

    // la file d'attente est en mémoire : après un redémarrage, les analyses en cours sont perdues
    @EventListener(ApplicationReadyEvent.class)
    public void marquerTraitementsInterrompus() {
        int nombre = documentRepository.changerStatut(
                List.of(DocumentStatus.UPLOADED, DocumentStatus.PROCESSING),
                DocumentStatus.FAILED,
                "Traitement interrompu, relance l'analyse du document");
        if (nombre > 0) {
            log.warn("{} document(s) en attente ou en cours ont été marqués FAILED au démarrage", nombre);
        }
    }
}