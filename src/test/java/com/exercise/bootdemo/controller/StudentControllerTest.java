package com.exercise.bootdemo.controller;

import com.exercise.bootdemo.dto.StudentDTO;
import com.exercise.bootdemo.exception.StudentNotFoundException;
import com.exercise.bootdemo.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests fuer die HTTP-Schicht.
 *
 * Geprueft werden hier die Dinge, die in einem Service-Test nicht
 * sichtbar werden: Statuscodes, das Antwortformat von Response und
 * die Validierung des Request-Bodys.
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private StudentService studentService;

    private static StudentDTO validDto() {
        StudentDTO dto = new StudentDTO();
        dto.setName("Yutong Qian");
        dto.setEmail("yutong@example.com");
        dto.setAge(30);
        return dto;
    }

    @Test
    @DisplayName("GET /student/{id} liefert 200 und success=true")
    void getReturnsOk() throws Exception {
        given(studentService.getStudentById(1L)).willReturn(validDto());

        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("yutong@example.com"));
    }

    @Test
    @DisplayName("GET /student/{id} liefert 404, wenn der Service nicht findet")
    void getReturnsNotFound() throws Exception {
        given(studentService.getStudentById(99L)).willThrow(new StudentNotFoundException(99L));

        mockMvc.perform(get("/student/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMsg").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("POST /student liefert 400 bei leerem Body, ohne den Service zu rufen")
    void postRejectsEmptyBody() throws Exception {
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // Die Validierung muss vor der Fachlogik greifen
        verify(studentService, never()).addNewStudent(any());
    }

    @Test
    @DisplayName("POST /student liefert 400 bei ungueltiger E-Mail")
    void postRejectsInvalidEmail() throws Exception {
        StudentDTO dto = validDto();
        dto.setEmail("keine-email");

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(studentService, never()).addNewStudent(any());
    }

    @Test
    @DisplayName("POST /student liefert 200 und die neue ID")
    void postCreatesAndReturnsId() throws Exception {
        given(studentService.addNewStudent(any(StudentDTO.class))).willReturn(42L);

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    @DisplayName("DELETE /student/{id} liefert 200 und ein Response-Objekt")
    void deleteReturnsResponse() throws Exception {
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(studentService).deleteStudentById(1L);
    }

    @Test
    @DisplayName("PUT /student/{id} akzeptiert einen JSON-Body")
    void putAcceptsJsonBody() throws Exception {
        given(studentService.updateStudentById(anyLong(), any(), any())).willReturn(validDto());

        mockMvc.perform(put("/student/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(validDto())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
