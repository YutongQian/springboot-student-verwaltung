package com.exercise.bootdemo.exception;

import org.springframework.http.HttpStatus;

/**
 * Wird geworfen, wenn eine Entitaet mit der angegebenen ID nicht existiert.
 * Antwortet mit HTTP 404.
 */
public class StudentNotFoundException extends ApiException {

    public StudentNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Student mit der ID " + id + " wurde nicht gefunden");
    }
}
