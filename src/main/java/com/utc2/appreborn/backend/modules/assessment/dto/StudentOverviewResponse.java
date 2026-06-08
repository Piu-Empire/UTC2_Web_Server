package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StudentOverviewResponse {
    private Long userId;
    private String studentCode; // MSSV
    private String periodId;
    private BigDecimal studentTotalScore;
    private BigDecimal tapTheScore;
    private BigDecimal boMonScore;
    private BigDecimal khoaScore;
    private BigDecimal truongScore;
    private boolean advisorApproved;
    private boolean khoaApproved;
    private boolean truongApproved;
    private LocalDateTime advisorApprovedAt;
    private LocalDateTime khoaApprovedAt;
    private LocalDateTime truongApprovedAt;
    private LocalDateTime submittedAt;
    private List<CriteriaDetail> criteriaDetails;

    @Data
    @Builder
    public static class CriteriaDetail {
        private int criteriaId;
        private BigDecimal studentScore;
        private List<String> evidenceUris;
        private BigDecimal tapTheScore;
        private BigDecimal boMonScore;
        private BigDecimal khoaScore;
        private BigDecimal truongScore;
    }
}