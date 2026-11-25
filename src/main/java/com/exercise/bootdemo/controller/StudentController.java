package com.exercise.bootdemo.controller;

import com.exercise.bootdemo.Response;
import com.exercise.bootdemo.dto.StudentDTO;
import com.exercise.bootdemo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-Endpunkte fuer Studenten.
 *
 * Aenderungen gegenueber der ersten Fassung:
 * - Konstruktor-Injektion statt @Autowired
 * - @Valid auf dem Request-Body (sonst greift die Validierung im DTO nicht)
 * - PUT nimmt einen Body statt @RequestParam entgegen (REST-konform,
 *   funktioniert auch mit Umlauten und Sonderzeichen zuverlaessig)
 * - DELETE liefert ein Response statt void, damit alle Endpunkte
 *   dasselbe Format haben
 *
 * Ergaenzt fuer die Weboberflaeche (index.html):
 * - GET /student liefert alle Studenten (ohne diesen Endpunkt muesste
 *   die Oberflaeche raten, welche IDs existieren)
 * - PUT gibt das ganze DTO an den Service weiter, damit auch age
 *   aktualisiert wird
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public Response<List<StudentDTO>> getAllStudents() {
        return Response.newSuccess(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public Response<StudentDTO> getStudentById(@PathVariable long id) {
        return Response.newSuccess(studentService.getStudentById(id));
    }

    @PostMapping
    public Response<Long> addNewStudent(@Valid @RequestBody StudentDTO studentDTO) {
        return Response.newSuccess(studentService.addNewStudent(studentDTO));
    }

    @DeleteMapping("/{id}")
    public Response<Void> deleteStudentById(@PathVariable long id) {
        studentService.deleteStudentById(id);
        return Response.newSuccess(null);
    }

    @PutMapping("/{id}")
    public Response<StudentDTO> updateStudentById(@PathVariable long id,
                                                  @Valid @RequestBody StudentDTO studentDTO) {
        return Response.newSuccess(studentService.updateStudentById(id, studentDTO));
    }
}

