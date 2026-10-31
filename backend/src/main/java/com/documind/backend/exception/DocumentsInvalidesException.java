package com.documind.backend.exception;

public class DocumentsInvalidesException extends RuntimeException {

    public DocumentsInvalidesException() {
        super("Un ou plusieurs documents sont introuvables ou pas encore prêts");
    }
}