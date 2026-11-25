package com.exercise.bootdemo.service;

import com.exercise.bootdemo.dao.Student;
import com.exercise.bootdemo.dao.StudentRepository;
import com.exercise.bootdemo.dto.StudentDTO;
import com.exercise.bootdemo.exception.EmailAlreadyUsedException;
import com.exercise.bootdemo.exception.StudentNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-Tests fuer die Fachlogik.
 *
 * Diese Tests laufen ohne Spring und ohne Datenbank: das Repository wird
 * durch einen Mock ersetzt. Dadurch dauert der gesamte Testlauf
 * Millisekunden statt Sekunden.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private static Student studentWith(long id, String name, String email) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setEmail(email);
        return student;
    }

    @Test
    @DisplayName("getStudentById liefert das DTO, wenn der Student existiert")
    void getStudentByIdReturnsDto() {
        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(studentWith(1L, "Yutong Qian", "yutong@example.com")));

        StudentDTO result = studentService.getStudentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Yutong Qian");
        assertThat(result.getEmail()).isEqualTo("yutong@example.com");
    }

    @Test
    @DisplayName("getStudentById wirft StudentNotFoundException, wenn die ID unbekannt ist")
    void getStudentByIdThrowsWhenMissing() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(StudentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("addNewStudent speichert, wenn die E-Mail noch frei ist")
    void addNewStudentSavesWhenEmailIsFree() {
        StudentDTO dto = new StudentDTO();
        dto.setName("Neue Studentin");
        dto.setEmail("neu@example.com");

        when(studentRepository.findByEmail("neu@example.com")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class)))
                .thenReturn(studentWith(7L, "Neue Studentin", "neu@example.com"));

        Long id = studentService.addNewStudent(dto);

        assertThat(id).isEqualTo(7L);
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("addNewStudent wirft EmailAlreadyUsedException, wenn die E-Mail vergeben ist")
    void addNewStudentThrowsOnDuplicateEmail() {
        StudentDTO dto = new StudentDTO();
        dto.setName("Doppelt");
        dto.setEmail("belegt@example.com");

        when(studentRepository.findByEmail("belegt@example.com"))
                .thenReturn(Optional.of(studentWith(1L, "Erster", "belegt@example.com")));

        assertThatThrownBy(() -> studentService.addNewStudent(dto))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("belegt@example.com");

        // Wichtig: bei doppelter E-Mail darf nicht gespeichert werden
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("deleteStudentById loescht, wenn der Student existiert")
    void deleteStudentByIdDeletesExisting() {
        Student existing = studentWith(3L, "Zu loeschen", "weg@example.com");
        when(studentRepository.findById(3L)).thenReturn(Optional.of(existing));

        studentService.deleteStudentById(3L);

        verify(studentRepository).delete(existing);
    }

    @Test
    @DisplayName("deleteStudentById wirft StudentNotFoundException bei unbekannter ID")
    void deleteStudentByIdThrowsWhenMissing() {
        when(studentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteStudentById(42L))
                .isInstanceOf(StudentNotFoundException.class);

        verify(studentRepository, never()).deleteById(anyLong());
        verify(studentRepository, never()).delete(any(Student.class));
    }

    @Test
    @DisplayName("updateStudentById aendert nur die uebergebenen Felder")
    void updateStudentByIdChangesOnlyGivenFields() {
        Student existing = studentWith(5L, "Alter Name", "alt@example.com");
        when(studentRepository.findById(5L)).thenReturn(Optional.of(existing));

        StudentDTO patch = new StudentDTO();
        patch.setName("Neuer Name");

        StudentDTO result = studentService.updateStudentById(5L, patch);

        assertThat(result.getName()).isEqualTo("Neuer Name");
        // Die E-Mail wurde nicht uebergeben und darf unveraendert bleiben
        assertThat(result.getEmail()).isEqualTo("alt@example.com");
    }

    @Test
    @DisplayName("updateStudentById ignoriert leere Strings statt sie zu speichern")
    void updateStudentByIdIgnoresBlankValues() {
        Student existing = studentWith(6L, "Bleibt", "bleibt@example.com");
        when(studentRepository.findById(6L)).thenReturn(Optional.of(existing));

        StudentDTO patch = new StudentDTO();
        patch.setName("   ");
        patch.setEmail("");

        StudentDTO result = studentService.updateStudentById(6L, patch);

        assertThat(result.getName()).isEqualTo("Bleibt");
        assertThat(result.getEmail()).isEqualTo("bleibt@example.com");
    }

    @Test
    @DisplayName("updateStudentById aendert auch das Alter")
    void updateStudentByIdChangesAge() {
        Student existing = studentWith(8L, "Mit Alter", "alter@example.com");
        existing.setAge(30);
        when(studentRepository.findById(8L)).thenReturn(Optional.of(existing));

        StudentDTO patch = new StudentDTO();
        patch.setAge(31);

        StudentDTO result = studentService.updateStudentById(8L, patch);

        assertThat(result.getAge()).isEqualTo(31);
        // Name und E-Mail bleiben, weil sie nicht im DTO standen
        assertThat(result.getName()).isEqualTo("Mit Alter");
        assertThat(result.getEmail()).isEqualTo("alter@example.com");
    }

    @Test
    @DisplayName("getAllStudents liefert alle Eintraege als DTO-Liste")
    void getAllStudentsReturnsAll() {
        Student first = studentWith(1L, "Erste", "eins@example.com");
        first.setAge(22);
        Student second = studentWith(2L, "Zweite", "zwei@example.com");
        second.setAge(25);

        when(studentRepository.findAll()).thenReturn(List.of(first, second));

        List<StudentDTO> result = studentService.getAllStudents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Erste");
        assertThat(result.get(1).getEmail()).isEqualTo("zwei@example.com");
    }

    @Test
    @DisplayName("getAllStudents liefert eine leere Liste, wenn nichts gespeichert ist")
    void getAllStudentsReturnsEmptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());

        assertThat(studentService.getAllStudents()).isEmpty();
    }
}
