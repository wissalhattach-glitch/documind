package com.documind.backend.exception;

public class TraitementEnCoursException extends RuntimeException {

    public TraitementEnCoursException() {
        super("Ce document est en cours d'analyse, réessaie dans un instant");
    }
}