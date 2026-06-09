package com.utc2.appreborn.backend.modules.aichat.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String message;
    private List<ChatMessageDto> conversation;
    private String action;
    private String actionId;
}
