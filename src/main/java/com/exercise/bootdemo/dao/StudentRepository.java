package com.exercise.bootdemo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Datenzugriff fuer Student.
 * findByEmail liefert Optional, weil die E-Mail fachlich eindeutig ist
 * (siehe Unique-Constraint in der Datenbank).
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);
}

