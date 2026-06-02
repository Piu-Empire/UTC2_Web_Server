package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Data;

@Data
public class ScholarshipUpsertDto {
    private Long   userId;         // sinh viên nhận học bổng
    private Long   scholarshipId;  // học bổng nào
    private String status;         // received | not_received
    private Long   semesterId;
    private String receivedAt;
}