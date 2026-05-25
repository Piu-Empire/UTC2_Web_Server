package com.utc2.appreborn.backend.modules.interaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackRequest {

    @NotBlank(message = "Loại phản hồi không được để trống")
    @Size(max = 50)
    private String type;   // "Lỗi" | "Góp ý"

    @NotBlank(message = "Nội dung không được để trống")
    private String content;
}