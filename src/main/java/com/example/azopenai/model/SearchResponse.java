package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for similarity search.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {

    /** List of matched documents. */
    private List<SearchResult> results;

    /** Total number of results. */
    private int totalResults;

    /** Processing time in milliseconds. */
    private long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String documentId;
        private String content;
        private Map<String, Object> metadata;
        private Double score;
    }
}
