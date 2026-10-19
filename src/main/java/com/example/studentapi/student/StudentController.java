package com.example.studentapi.student;

import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
public class StudentController {

    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<StudentResponse> getStudents() {
        return studentRepository
                .findAll(Sort.by(
                        Sort.Order.asc("lastName"),
                        Sort.Order.asc("firstName")))
                .stream()
                .map(StudentResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<StudentResponse> addStudent(
            @Valid @RequestBody StudentRequest request) {

        String patronymic = request.patronymic();

        if (patronymic != null && patronymic.isBlank()) {
            patronymic = null;
        } else if (patronymic != null) {
            patronymic = patronymic.trim();
        }

        Student student = new Student(
                request.firstName().trim(),
                request.lastName().trim(),
                patronymic,
                request.group().trim());

        Student saved = studentRepository.save(student);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StudentResponse.from(saved));
    }
}
