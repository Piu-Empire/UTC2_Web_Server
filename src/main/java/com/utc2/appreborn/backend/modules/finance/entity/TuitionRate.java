package com.utc2.appreborn.backend.modules.finance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tuition_rate")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "price_per_credit", nullable = false, precision = 15, scale = 2)
    private BigDecimal pricePerCredit;

    /** Năm học áp dụng — null nghĩa là dùng làm mặc định */
    @Column(name = "academic_year")
    private String academicYear;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "note")
    private String note;
}