package com.utc2.appreborn.backend.modules.academic.dto;

import lombok.Data;

@Data
public class GradeUpdateDto {
    private Double midtermScore;
    private Double finalScore;
    private Double assignmentScore;
}
