package com.utc2.appreborn.backend.modules.aichat.controller;

import com.utc2.appreborn.backend.modules.aichat.dto.ChatRequest;
import com.utc2.appreborn.backend.modules.aichat.dto.ChatResponse;
import com.utc2.appreborn.backend.modules.aichat.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> processMessage(@RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
