package com.documind.backend.service;

// "un document est prêt à être analysé"
public record DocumentImporteEvent(Long documentId, Long userId) {
}