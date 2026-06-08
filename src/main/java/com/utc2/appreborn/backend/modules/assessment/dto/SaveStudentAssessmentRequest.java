package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// ─── Request: App gửi lên để lưu đánh giá sinh viên ─────────────────────────

@Data
public class SaveStudentAssessmentRequest {
    private String periodId;
    private List<CriteriaScoreItem> items;

    @Data
    public static class CriteriaScoreItem {
        private int criteriaId;
        private BigDecimal score;
        private List<String> evidenceUris; // có thể null
    }
}