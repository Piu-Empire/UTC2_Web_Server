package com.utc2.appreborn.backend.modules.aichat.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ChatKnowledgeRepository {

    @Data
    public static class KnowledgeItem {
        private String id;
        private List<String> questions;
        private String answer;
        private String actionId;
    }

    private final List<KnowledgeItem> knowledgeItems = new ArrayList<>();

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("chat_knowledge_base.json").getInputStream();
            JsonNode rootNode = mapper.readTree(is);
            JsonNode matches = rootNode.get("exact_matches");
            if (matches != null && matches.isArray()) {
                for (JsonNode node : matches) {
                    KnowledgeItem item = new KnowledgeItem();
                    item.setId(node.get("id").asText());
                    item.setAnswer(node.get("answer").asText());
                    if (node.has("actionId")) {
                        item.setActionId(node.get("actionId").asText());
                    }
                    List<String> questions = new ArrayList<>();
                    for (JsonNode qNode : node.get("questions")) {
                        questions.add(qNode.asText().toLowerCase());
                    }
                    item.setQuestions(questions);
                    knowledgeItems.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load chat_knowledge_base.json: " + e.getMessage());
        }
    }

    public Optional<KnowledgeItem> findById(String id) {
        return knowledgeItems.stream()
            .filter(item -> item.getId().equals(id))
            .findFirst();
    }

    public Optional<KnowledgeItem> findExactMatch(String query) {
        String lowerQuery = query.toLowerCase().trim();
        return knowledgeItems.stream()
            .filter(item -> item.getQuestions().contains(lowerQuery))
            .findFirst();
    }

    public List<String> findSuggestions(String query) {
        String lowerQuery = query.toLowerCase().trim();
        List<String> suggestions = new ArrayList<>();
        
        for (KnowledgeItem item : knowledgeItems) {
            for (String q : item.getQuestions()) {
                if (q.contains(lowerQuery)) {
                    suggestions.add(q);
                    // Lấy max 3 gợi ý
                    if (suggestions.size() >= 3) return suggestions;
                }
            }
        }
        return suggestions;
    }
}
