package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private Integer rank;
    private String  studentCode;
    private String  fullName;
    private String  initials;
    private Integer totalCredits;
    private Double  gpa;
    private Boolean isCurrentUser;
}
