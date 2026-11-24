package com.exercise.bootdemo.exception;

import org.springframework.http.HttpStatus;

/**
 * Basisklasse fuer alle fachlichen Fehler der Anwendung.
 * Traegt den HTTP-Status mit, damit der Handler ihn nicht raten muss.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
