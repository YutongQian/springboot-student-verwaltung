package com.exercise.bootdemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datenobjekt fuer den Austausch mit dem Client.
 * Bewusst ohne Bean-Validation-Annotationen auf dem Entity selbst:
 * die Regeln der API sind nicht identisch mit den Regeln der Datenbank.
 */
public class StudentDTO {

    private Long id;

    @NotBlank(message = "darf nicht leer sein")
    @Size(max = 100, message = "darf hoechstens 100 Zeichen lang sein")
    private String name;

    @NotBlank(message = "darf nicht leer sein")
    @Email(message = "muss eine gueltige E-Mail-Adresse sein")
    private String email;

    @Positive(message = "muss groesser als 0 sein")
    private Integer age;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}

