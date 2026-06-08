package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Response trả về đánh giá CVHT (sinh viên đánh giá cố vấn)
@Data
@Builder
public class AdvisorAssessmentResponse {
    private Long userId;
    private String periodId;
    private String studentOpinion; // Ý kiến tự do của sinh viên về CVHT
    private List<CriteriaScoreDto> items;
    private LocalDateTime submittedAt;

    @Data
    @Builder
    public static class CriteriaScoreDto {
        private int criteriaId;
        private BigDecimal score;
    }
}