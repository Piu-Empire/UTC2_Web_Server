package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// Response: App lấy điểm readonly Tập thể lớp / Khoa/BM / Trường

@Data
@Builder
public class ExternalAssessmentResponse {
    private Long userId;
    private String periodId;
    private List<ExternalScoreDto> items;

    // Trạng thái duyệt — App dùng để show/hide tab
    private boolean advisorApproved;
    private boolean khoaApproved;
    private boolean truongApproved;

    @Data
    @Builder
    public static class ExternalScoreDto {
        private int criteriaId;
        private BigDecimal tapTheScore;
        private BigDecimal boMonScore;
        private BigDecimal khoaScore;
        private BigDecimal truongScore;
    }
}