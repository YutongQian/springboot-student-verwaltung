package com.exercise.bootdemo.service;

import com.exercise.bootdemo.dao.Student;
import com.exercise.bootdemo.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    StudentDTO getStudentById(long id);

    /**
     * Liefert alle Studenten. Wird von der Weboberflaeche gebraucht,
     * damit die Tabelle ohne bekannte IDs gefuellt werden kann.
     */
    List<StudentDTO> getAllStudents();

    Long addNewStudent(StudentDTO studentDTO);

    void deleteStudentById(long id);

    /**
     * Aktualisiert die uebergebenen Felder. Das DTO statt einzelner
     * Parameter, damit age nicht vergessen wird und weitere Felder
     * spaeter ohne Signaturwechsel dazukommen koennen.
     */
    StudentDTO updateStudentById(long id, StudentDTO studentDTO);
}
