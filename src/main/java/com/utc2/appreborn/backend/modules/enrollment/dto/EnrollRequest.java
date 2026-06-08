package com.utc2.appreborn.backend.modules.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollRequest {

    @NotNull(message = "courseId không được để trống")
    private Long courseId;

    @NotNull(message = "semesterId không được để trống")
    private Long semesterId;
}
