package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryDto {
    private Integer rank;
    private String  studentCode;
    private String  fullName;
    private String  initials;
    private Integer totalCredits;
    private Double  gpa;
    private Boolean isCurrentUser;
}