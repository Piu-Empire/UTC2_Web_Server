package com.utc2.appreborn.backend.modules.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionButtonDto {
    private String type; // e.g., CONFIRM_CORRECT, CONFIRM_WRONG, SUGGESTION
    private String label; // e.g., "✅ Chính xác"
    private String data; // Optional data, like document ID or suggestion ID
}
