package com.utc2.appreborn.backend.modules.public_services.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranscriptRequest {
    @NotBlank(message = "Vui lòng chọn năm học")
    private String academicYear;    // dropAcademicYear

    @NotBlank(message = "Vui lòng chọn học kỳ")
    private String semester;        // dropSemester

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private int quantity;           // edtQuantity

    private String note;            // edtNote
}