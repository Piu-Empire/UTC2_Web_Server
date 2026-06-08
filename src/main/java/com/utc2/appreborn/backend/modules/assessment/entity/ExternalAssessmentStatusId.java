package com.utc2.appreborn.backend.modules.assessment.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAssessmentStatusId implements Serializable {
    private Long userId;
    private String periodId;
}