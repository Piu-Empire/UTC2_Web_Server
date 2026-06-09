package com.utc2.appreborn.backend.modules.aichat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {
    private String type; // suggestions, answer, clarify, not_found, calculation
    private String message; 
    private List<String> items; // for suggestions
    private String source; // for answer
    private String content; // for answer
    private String actionId; // for answer (navigation)
    private List<String> options; // for clarify
    
    // For calculation
    private String id;
    private String expression;
    private List<Double> numbers;
    private Double result;

    // For Semantic Search / RAG
    private String documentTitle;
    private String documentSource;
    private Double confidenceScore;
    
    // Dynamic Action Buttons Template
    private List<ActionButtonDto> actionButtons;
}
