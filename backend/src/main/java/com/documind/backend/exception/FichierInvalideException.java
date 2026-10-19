package com.documind.backend.exception;

public class FichierInvalideException extends RuntimeException {
    public FichierInvalideException(String message) {
        super(message);
    }
}