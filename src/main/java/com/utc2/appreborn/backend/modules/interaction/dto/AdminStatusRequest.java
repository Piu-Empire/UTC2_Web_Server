package com.utc2.appreborn.backend.modules.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminStatusRequest {

    @NotBlank
    private String status;   // "đã đọc" | "đã phản hồi"
}