package com.utc2.appreborn.backend.modules.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminReplyRequest {

    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String adminReply;
}