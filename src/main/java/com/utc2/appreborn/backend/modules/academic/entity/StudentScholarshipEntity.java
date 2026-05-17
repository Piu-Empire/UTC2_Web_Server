package com.utc2.appreborn.backend.modules.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_scholarship")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScholarshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "scholarship_id", nullable = false)
    private Long scholarshipId;

    @Column(name = "status")
    private String status;

    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "received_at")
    private LocalDate receivedAt;
}