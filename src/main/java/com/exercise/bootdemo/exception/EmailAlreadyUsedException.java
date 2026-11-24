package com.exercise.bootdemo.exception;

import org.springframework.http.HttpStatus;

/**
 * Wird geworfen, wenn eine E-Mail-Adresse bereits vergeben ist.
 * Antwortet mit HTTP 409 (Conflict).
 */
public class EmailAlreadyUsedException extends ApiException {

    public EmailAlreadyUsedException(String email) {
        super(HttpStatus.CONFLICT, "Die E-Mail-Adresse " + email + " ist bereits vergeben");
    }
}
