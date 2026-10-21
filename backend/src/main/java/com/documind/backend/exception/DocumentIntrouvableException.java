package com.documind.backend.exception;

public class DocumentIntrouvableException extends RuntimeException {
    public DocumentIntrouvableException() {
        super("Document introuvable");
    }
}