package com.exercise.bootdemo.converter;

import com.exercise.bootdemo.dao.Student;
import com.exercise.bootdemo.dto.StudentDTO;

/**
 * Wandelt zwischen Entity und DTO.
 * Bewusst ohne Framework: eine statische Utility-Klasse reicht hier,
 * weil keine Abhaengigkeiten injiziert werden muessen.
 */
public final class StudentConverter {

    private StudentConverter() {
        // Utility-Klasse, nicht instanziierbar
    }

    public static StudentDTO convertStudent(Student student) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(student.getId());
        studentDTO.setName(student.getName());
        studentDTO.setEmail(student.getEmail());
        studentDTO.setAge(student.getAge());
        return studentDTO;
    }

    public static Student convertStudent(StudentDTO studentDTO) {
        Student student = new Student();
        student.setName(studentDTO.getName());
        student.setEmail(studentDTO.getEmail());
        if (studentDTO.getAge() != null) {
            student.setAge(studentDTO.getAge());
        }
        return student;
    }
}

