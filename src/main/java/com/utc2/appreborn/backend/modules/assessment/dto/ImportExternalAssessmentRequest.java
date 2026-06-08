package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Request: Admin import điểm Tập thể lớp / Khoa/BM / Trường cho một sinh viên

@Data
public class ImportExternalAssessmentRequest {
    private Long userId;
    private String periodId;
    private List<ExternalScoreItem> items;

    @Data
    public static class ExternalScoreItem {
        private int criteriaId;
        private BigDecimal tapTheScore;
        private BigDecimal boMonScore;
        private BigDecimal khoaScore;
        private BigDecimal truongScore;
    }
}