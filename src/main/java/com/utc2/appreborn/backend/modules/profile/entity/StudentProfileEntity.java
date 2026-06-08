package com.utc2.appreborn.backend.modules.profile.entity;

import com.utc2.appreborn.backend.modules.auth.entity.User;
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

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "student_code", unique = true)
    private String studentCode;

    @Column(name = "faculty")
    private String faculty;

   @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id") // Ánh xạ tới cột advisor_id trong bảng student_profile
    private AdvisorEntity advisor;

    @Column(name = "major")
    private String major;

    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "class_name")
    private String className;

    @Column(name = "status")
    private String status;

    @Column(name = "student_card_url", columnDefinition = "TEXT")
    private String studentCardUrl;
}