package com.documind.backend.exception;

import com.documind.backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<ErrorResponse> emailDejaUtilise() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "EMAIL_ALREADY_USED", "Cet email est déjà utilisé"));
    }

    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ErrorResponse> identifiantsInvalides() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(401, "INVALID_CREDENTIALS", "Email ou mot de passe incorrect"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> donneesInvalides(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "VALIDATION_ERROR", detail));
    }

    @ExceptionHandler(FichierInvalideException.class)
    public ResponseEntity<ErrorResponse> fichierInvalide(FichierInvalideException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "INVALID_FILE", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> fichierTropGros() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(413, "FILE_TOO_LARGE", "Fichier trop volumineux (50 Mo maximum)"));
    }

    @ExceptionHandler(DocumentIntrouvableException.class)
    public ResponseEntity<ErrorResponse> documentIntrouvable() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "DOCUMENT_NOT_FOUND", "Document introuvable"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> parametreInvalide(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(400, "INVALID_PARAMETER", "Paramètre invalide : " + e.getName()));
    }
    @ExceptionHandler(ServiceIaIndisponibleException.class)
    public ResponseEntity<ErrorResponse> handleServiceIa(ServiceIaIndisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(503, "AI_UNAVAILABLE", e.getMessage()));
    }
}