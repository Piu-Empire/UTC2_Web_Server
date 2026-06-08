package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Data;

/** Dùng chung cho approve leaderboard, scholarship, warning */
@Data
public class ApprovalDto {
    private Long semesterId;      // cho leaderboard
    private Long targetId;        // scholarshipId hoặc warningId
    private Long userId;          // userId sinh viên (scholarship/warning)
}