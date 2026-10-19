package com.example.studentapi.student;

import org.springframework.web.util.HtmlUtils;

public record StudentResponse(
        Long id,
        String firstName,
        String lastName,
        String patronymic,
        String group) {

    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                escape(student.getFirstName()),
                escape(student.getLastName()),
                escape(student.getPatronymic()),
                escape(student.getGroup()));
    }

    private static String escape(String value) {
        return value == null ? null : HtmlUtils.htmlEscape(value);
    }
}
