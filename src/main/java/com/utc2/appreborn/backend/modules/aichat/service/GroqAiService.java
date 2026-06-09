package com.utc2.appreborn.backend.modules.aichat.service;

import com.utc2.appreborn.backend.modules.aichat.dto.ChatMessageDto;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GroqAiService {

    @org.springframework.beans.factory.annotation.Value("${groq.api.key}")
    private String GROQ_KEY;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Data
    public static class ModelConfig {
        private String id;
        private int rpd;

        public ModelConfig(String id, int rpd) {
            this.id = id;
            this.rpd = rpd;
        }
    }

    private final List<ModelConfig> MODELS = Arrays.asList(
            new ModelConfig("llama-3.1-8b-instant", 14400),
            new ModelConfig("meta-llama/llama-prompt-guard-2-22m", 14400),
            new ModelConfig("meta-llama/llama-prompt-guard-2-86m", 14400),
            new ModelConfig("allam-2-7b", 7000),
            new ModelConfig("llama-3.3-70b-versatile", 1000),
            new ModelConfig("meta-llama/llama-4-scout-17b-16e-instruct", 1000),
            new ModelConfig("qwen/qwen3-32b", 1000),
            new ModelConfig("openai/gpt-oss-20b", 1000),
            new ModelConfig("openai/gpt-oss-120b", 1000),
            new ModelConfig("openai/gpt-oss-safeguard-20b", 1000), // added
            new ModelConfig("groq/compound", 250), // added
            new ModelConfig("groq/compound-mini", 250) // added
    );

    @Data
    public static class ModelState {
        private boolean blocked = false;
        private Long blockedAt = null;
    }

    private final Map<String, ModelState> modelStateMap = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public GroqAiService() {
        for (ModelConfig m : MODELS) {
            modelStateMap.put(m.getId(), new ModelState());
        }
    }

    private void unblockIfExpired() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ModelState> entry : modelStateMap.entrySet()) {
            ModelState s = entry.getValue();
            if (s.isBlocked() && s.getBlockedAt() != null && (now - s.getBlockedAt()) > 60000) {
                s.setBlocked(false);
                System.out.println("Model " + entry.getKey() + " unblocked, thử lại.");
            }
        }
    }

    public String chatWithFallback(List<ChatMessageDto> conversation, String systemPrompt, String newMessage) {
        unblockIfExpired();

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);
        }

        if (conversation != null) {
            for (ChatMessageDto msg : conversation) {
                Map<String, String> m = new HashMap<>();
                m.put("role", msg.isUser() ? "user" : "assistant");
                m.put("content", msg.getText());
                messages.add(m);
            }
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", newMessage);
        messages.add(userMsg);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + GROQ_KEY);

        for (ModelConfig model : MODELS) {
            String id = model.getId();
            ModelState state = modelStateMap.get(id);
            if (state.isBlocked())
                continue;

            Map<String, Object> body = new HashMap<>();
            body.put("model", id);
            body.put("messages", messages);
            body.put("max_tokens", 1024);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
                        return (String) messageMap.get("content");
                    }
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429) {
                    state.setBlocked(true);
                    state.setBlockedAt(System.currentTimeMillis());
                    System.out.println("[429] " + id + " bị limit, chuyển sang model khác...");
                    continue;
                }
                if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 404) {
                    state.setBlocked(true);
                    state.setBlockedAt(System.currentTimeMillis());
                    continue;
                }
                // Other client errors -> throw if it's something fundamentally wrong like
                // unauthorized
                throw new RuntimeException("API Client Error: " + e.getMessage());
            } catch (HttpServerErrorException e) {
                if (e.getStatusCode().value() == 503) {
                    state.setBlocked(true);
                    state.setBlockedAt(System.currentTimeMillis());
                    continue;
                }
            } catch (Exception e) {
                // Lỗi mạng hoặc lỗi không xác định
                state.setBlocked(true);
                state.setBlockedAt(System.currentTimeMillis());
                continue;
            }
        }

        throw new RuntimeException("Tất cả model đã bị rate limit. Reset lúc 7h sáng (UTC+7).");
    }
}
