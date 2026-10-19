package com.example.studentapi.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRequest(

        @NotBlank @Size(max = 100) String firstName,

        @NotBlank @Size(max = 100) String lastName,

        @Size(max = 100) String patronymic,

        @NotBlank @Size(max = 50) String group

) {
}
