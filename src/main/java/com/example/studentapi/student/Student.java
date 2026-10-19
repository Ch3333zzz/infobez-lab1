package com.example.studentapi.student;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 100)
    private String patronymic;

    @Column(name = "student_group", nullable = false, length = 50)
    private String group;

    protected Student() {
    }

    public Student(
            String firstName,
            String lastName,
            String patronymic,
            String group) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.group = group;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public String getGroup() {
        return group;
    }
}
