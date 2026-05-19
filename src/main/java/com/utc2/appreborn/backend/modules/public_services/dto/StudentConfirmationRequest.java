package com.utc2.appreborn.backend.modules.public_services.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentConfirmationRequest {

    @NotBlank(message = "Vui lòng nhập lý do xin giấy xác nhận")
    private String purpose;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private int quantity = 1;
}