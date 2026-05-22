package com.utc2.appreborn.backend.modules.imports.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ImportResultResponse {
    private int success;
    private int failed;
    private List<ImportError> errors;

    @Data
    @Builder
    public static class ImportError {
        private int row;
        private String field;
        private String message;
    }
}