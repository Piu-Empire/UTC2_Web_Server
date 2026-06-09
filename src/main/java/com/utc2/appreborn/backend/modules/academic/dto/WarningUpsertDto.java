package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Data;

@Data
public class WarningUpsertDto {
    private Long   userId;      // (optional) sinh viên bị cảnh báo
    private String studentCode; // MSSV
    private Long   semesterId;
    private String warningType; // LOW_GPA | FAILED_EXAM | ATTENDANCE
    private String description;
    private String status;      // ACTIVE | RESOLVED
    private String resolvedAt;
}