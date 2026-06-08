package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_course")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherCourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "class_name")
    private String className; // NULL = dạy tất cả lớp

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}