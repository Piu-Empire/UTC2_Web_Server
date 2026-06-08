package com.utc2.appreborn.backend.modules.assessment.dto;

import lombok.Data;

@Data
public class ApproveRequest {
    private Long userId;
    private String periodId;
}