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

    /** Tổng tín chỉ đã đăng ký trong kỳ (status != 'đã hủy') — hiển thị "X TC" trên app */
    private Integer    totalCredits;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime paidAt;

    private String status;
    private String paymentMethod;
}
