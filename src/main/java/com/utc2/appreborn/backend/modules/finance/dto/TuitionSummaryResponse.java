package com.utc2.appreborn.backend.modules.finance.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TuitionSummaryResponse {
    private String studentId;
    private String fullName;
    private BigDecimal totalDebt;
    private List<TuitionResponse> semesters;
}