package com.exercise.bootdemo.service;

import com.exercise.bootdemo.dao.Student;
import com.exercise.bootdemo.dto.StudentDTO;

public interface StudentService {

    StudentDTO getStudentById(long id);

    Long addNewStudent(StudentDTO studentDTO);

    void deleteStudentById(long id);

    StudentDTO updateStudentById(long id, String name, String email);
}
