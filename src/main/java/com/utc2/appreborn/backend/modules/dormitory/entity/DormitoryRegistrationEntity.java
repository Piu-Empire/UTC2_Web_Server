package com.utc2.appreborn.backend.modules.dormitory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dormitory_registration")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DormitoryRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dorm_reg_id")
    private Long dormRegId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "registered_at", insertable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status")
    private String status;

    @Column(name = "total_fee", columnDefinition = "DECIMAL(15,2)")
    private Double totalFee;

    @Column(name = "paid_status")
    private String paidStatus;
}
