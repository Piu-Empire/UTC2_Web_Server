package com.utc2.appreborn.backend.modules.interaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackResponse {
    private Long   id;
    private String type;
    private String content;
    private String status;
    private String adminReply;

    // Thông tin sinh viên (cho admin)
    private String studentName;
    private String studentCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;
}