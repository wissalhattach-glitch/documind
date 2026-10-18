
package com.documind.backend.exception;

public class EmailDejaUtiliseException extends RuntimeException {
    public EmailDejaUtiliseException() {
        super("Email déjà utilisé");
    }
}