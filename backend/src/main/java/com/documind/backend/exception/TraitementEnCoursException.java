package com.documind.backend.exception;

public class TraitementEnCoursException extends RuntimeException {

    public TraitementEnCoursException() {
        super("Le document est en cours de traitement, réessaie dans un instant");
    }
}