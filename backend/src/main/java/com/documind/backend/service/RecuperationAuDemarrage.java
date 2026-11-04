package com.documind.backend.service;

import com.documind.backend.entity.Document;
import com.documind.backend.entity.DocumentStatus;
import com.documind.backend.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecuperationAuDemarrage implements ApplicationRunner {

    private final DocumentRepository documentRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Document> bloques = documentRepository.findAll().stream()
                .filter(d -> d.getStatut() == DocumentStatus.UPLOADED
                        || d.getStatut() == DocumentStatus.PROCESSING)
                .toList();

        bloques.forEach(d -> d.marquerEchec("Analyse interrompue, relance nécessaire"));
        documentRepository.saveAll(bloques);

        if (!bloques.isEmpty()) {
            log.warn("{} document(s) interrompu(s) marqué(s) FAILED", bloques.size());
        }
    }
}