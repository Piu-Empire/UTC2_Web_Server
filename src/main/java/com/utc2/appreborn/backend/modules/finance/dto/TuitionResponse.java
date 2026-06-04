package com.utc2.appreborn.backend.modules.finance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TuitionResponse {
    private Long       id;
    private String     studentId;
    private String     fullName;
    private Long       semesterId;
    private String     semesterName;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    // FIX BUG 3: @JsonFormat đảm bảo Jackson luôn serialize thành
    // ISO String "2024-05-10", không phải array [2024,5,10].
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    // FIX BUG 3: tương tự cho LocalDateTime paidAt.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paidAt;

    private String status;
    private String paymentMethod;
}