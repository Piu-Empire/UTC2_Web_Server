package com.utc2.appreborn.backend.modules.aichat.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatMessageDto {
    private String text;
    private boolean isUser;
}
