package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ─── Response App lấy điểm sinh viên tự đánh giá (theo học kỳ) ──────────────

@Data
@Builder
public class StudentAssessmentResponse {
    private Long userId;
    private String periodId;
    private List<CriteriaScoreDto> items;
    private LocalDateTime submittedAt;

    @Data
    @Builder
    public static class CriteriaScoreDto {
        private int criteriaId;
        private BigDecimal score;
        private List<String> evidenceUris;
    }
}