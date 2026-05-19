package com.utc2.appreborn.backend.modules.public_services.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoanSupportRequest {
    @NotBlank(message = "Vui lòng nhập số tiền cần vay")
    private String loanAmount;      // edtAmount

    @NotBlank(message = "Vui lòng nhập lý do vay")
    private String loanReason;      // edtReason

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;     // edtPhone
}