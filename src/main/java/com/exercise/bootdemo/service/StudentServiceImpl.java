package com.exercise.bootdemo.service;

import com.exercise.bootdemo.converter.StudentConverter;
import com.exercise.bootdemo.dao.Student;
import com.exercise.bootdemo.dao.StudentRepository;
import com.exercise.bootdemo.dto.StudentDTO;
import com.exercise.bootdemo.exception.EmailAlreadyUsedException;
import com.exercise.bootdemo.exception.StudentNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Fachlogik fuer Studenten.
 *
 * Konstruktor-Injektion statt @Autowired auf dem Feld: die Klasse ist
 * damit ohne Spring testbar (Repository kann per Mockito uebergeben
 * werden) und die Abhaengigkeit ist unveraenderlich.
 */
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO getStudentById(long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        return StudentConverter.convertStudent(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentConverter::convertStudent)
                .toList();
    }

    @Override
    @Transactional
    public Long addNewStudent(StudentDTO studentDTO) {
        Optional<Student> existing = studentRepository.findByEmail(studentDTO.getEmail());
        if (existing.isPresent()) {
            throw new EmailAlreadyUsedException(studentDTO.getEmail());
        }
        Student saved = studentRepository.save(StudentConverter.convertStudent(studentDTO));
        return saved.getId();
    }

    @Override
    @Transactional
    public void deleteStudentById(long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        studentRepository.delete(student);
    }

    @Override
    @Transactional
    public StudentDTO updateStudentById(long id, StudentDTO studentDTO) {
        Student studentInDb = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        if (StringUtils.hasText(studentDTO.getName())) {
            studentInDb.setName(studentDTO.getName());
        }
        if (StringUtils.hasText(studentDTO.getEmail())) {
            studentInDb.setEmail(studentDTO.getEmail());
        }
        // age ist ein int in der Entity und kann deshalb nicht "nicht gesetzt"
        // sein. Ein fehlender Wert (null im DTO) laesst das Feld unveraendert.
        if (studentDTO.getAge() != null) {
            studentInDb.setAge(studentDTO.getAge());
        }

        // Kein zusaetzliches save(): die Entitaet ist innerhalb der
        // Transaktion verwaltet, Hibernate schreibt die Aenderung beim
        // Commit. Ein save() waere hier ein unnoetiger Roundtrip.
        return StudentConverter.convertStudent(studentInDb);
    }
}

