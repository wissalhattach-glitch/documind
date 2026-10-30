package com.documind.backend.exception;

public class ServiceIaIndisponibleException extends RuntimeException {

    public ServiceIaIndisponibleException(String message) {
        super(message);
    }

    public ServiceIaIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}