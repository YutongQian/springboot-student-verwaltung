package com.exercise.bootdemo.exception;

import com.exercise.bootdemo.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Zentrale Fehlerbehandlung fuer alle Controller.
 *
 * Ohne diese Klasse liefert Spring fuer jede fachliche Ausnahme
 * einen nackten HTTP 500 mit einem Stacktrace im Log und ohne
 * verwertbare Information im Response-Body.
 *
 * Der Client bekommt hier immer eine Response mit success=false
 * und einer lesbaren Fehlermeldung.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Fachliche Fehler: 404, 409 usw. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Response<Void>> handleApiException(ApiException ex) {
        log.debug("Fachlicher Fehler: {} ({})", ex.getMessage(), ex.getStatus());
        return ResponseEntity.status(ex.getStatus())
                .body(Response.newFail(ex.getMessage()));
    }

    /** Validierungsfehler aus @Valid: HTTP 400 mit Feldnamen. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.debug("Validierung fehlgeschlagen: {}", message);
        return ResponseEntity.badRequest().body(Response.newFail(message));
    }

    /** Alles, was nicht vorhergesehen war: 500, aber ohne Stacktrace im Body. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<Void>> handleUnexpected(Exception ex) {
        log.error("Unerwarteter Fehler", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Response.newFail("Interner Fehler. Bitte kontaktieren Sie den Support."));
    }
}
