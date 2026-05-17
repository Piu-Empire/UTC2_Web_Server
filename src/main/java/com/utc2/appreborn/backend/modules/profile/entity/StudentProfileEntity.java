package com.utc2.appreborn.backend.modules.profile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "student_code")
    private String studentCode;

    @Column(name = "faculty")
    private String faculty;

    @Column(name = "advisor_id")
    private Long advisorId;

    @Column(name = "major")
    private String major;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "class_name")
    private String className;

    @Column(name = "status")
    private String status;
}