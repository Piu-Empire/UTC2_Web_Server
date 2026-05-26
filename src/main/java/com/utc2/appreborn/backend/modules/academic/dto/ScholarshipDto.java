package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDto {
    private Long   scholarshipId;
    private String name;
    private String organization;
    private Long   amount;
    private String unit;
    private Double minGpa;
    private String description;
    /** "received" | "not_received" | null */
    private String status;
    private String receivedAt;
}
