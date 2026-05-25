package com.utc2.appreborn.backend.modules.public_services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUpdateStatusRequest {

    /**
     * Trạng thái mới: PENDING | PROCESSING | COMPLETED | REJECTED
     */
    @NotBlank(message = "Trạng thái không được để trống")
    private String status;

    /**
     * Ghi chú kết quả xử lý (tuỳ chọn)
     */
    private String resultNote;
}