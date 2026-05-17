package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScholarshipDto {
    private Long   scholarshipId;
    private String name;
    private String organization;
    private Long   amount;
    private String unit;
    private java.math.BigDecimal minGpa;
    private String description;
    /** "received" | "not_received" | null (chưa được xét) */
    private String status;
    private String receivedAt;
}