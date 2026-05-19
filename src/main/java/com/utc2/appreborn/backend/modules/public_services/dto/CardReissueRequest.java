package com.utc2.appreborn.backend.modules.public_services.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardReissueRequest {
    // Lý do xin cấp lại thẻ (edtReason trong CardReissueActivity)
    private String reason;
}