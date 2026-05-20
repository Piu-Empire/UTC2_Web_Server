package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Request: App gửi lên để lưu đánh giá CVHT

@Data
public class SaveAdvisorAssessmentRequest {
    private String periodId;
    private List<CriteriaScoreItem> items;
    private String studentOpinion; // Ý kiến riêng (footer CVHT), có thể null

    @Data
    public static class CriteriaScoreItem {
        private int criteriaId;
        private BigDecimal score;
    }
}