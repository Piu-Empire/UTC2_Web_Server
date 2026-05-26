package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicWarningDto {
    private Long   warningId;
    private Long   semesterId;
    private String warningType;
    private String description;
    private String issuedAt;
    private String resolvedAt;
    private String status;
}
