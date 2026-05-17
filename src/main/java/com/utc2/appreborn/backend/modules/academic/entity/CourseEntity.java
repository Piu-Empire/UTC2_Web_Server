package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "course_code", unique = true)
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "theory_hours")
    private Integer theoryHours;

    @Column(name = "practice_hours")
    private Integer practiceHours;

    @Column(name = "department")
    private String department;

    @Column(name = "description")
    private String description;
}