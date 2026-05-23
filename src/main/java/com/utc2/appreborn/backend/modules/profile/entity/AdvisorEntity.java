package com.utc2.appreborn.backend.modules.profile.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "advisor")
@Data
public class AdvisorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advisor_id")
    private Long advisorId;

    private String fullName;
    private String email;
    private String phone;
    private String faculty;
}