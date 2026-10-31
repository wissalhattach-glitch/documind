package com.documind.backend.exception;

public class ConversationIntrouvableException extends RuntimeException {

    public ConversationIntrouvableException() {
        super("Conversation introuvable");
    }
}