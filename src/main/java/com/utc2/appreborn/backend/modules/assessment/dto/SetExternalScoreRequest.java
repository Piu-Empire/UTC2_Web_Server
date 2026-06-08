package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SetExternalScoreRequest {
    private Long userId;
    private String periodId;
    private List<CriteriaScore> items;

    @Data
    public static class CriteriaScore {
        private int criteriaId;
        private BigDecimal score;
    }
}