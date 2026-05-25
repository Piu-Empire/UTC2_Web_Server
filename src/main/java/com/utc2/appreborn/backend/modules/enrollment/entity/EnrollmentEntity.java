package com.utc2.appreborn.backend.modules.enrollment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment") // Tên bảng trong SQL
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;

    // Sửa các trường này cho khớp với cách gọi trong Service
    private Long userId;     // Phải có field này
    private Long courseId;   // Phải có field này
    private Long semesterId; // Phải có field này
    
    private String status;
    
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    // Nếu bạn dùng @Builder, Lombok sẽ tự sinh ra các phương thức userId(), courseId(), ...
}