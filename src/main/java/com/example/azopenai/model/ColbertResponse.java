package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for ColBERT operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColbertResponse {

    /** MaxSim score between query and document. */
    private double score;

    /** Number of query tokens. */
    private int queryTokenCount;

    /** Number of document tokens. */
    private int documentTokenCount;

    /** Per-token similarity scores. */
    private List<TokenScoreDetail> tokenScores;

    /** Processing time in milliseconds. */
    private long processingTimeMs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenScoreDetail {
        private String queryToken;
        private String bestMatchDocToken;
        private double similarity;
    }
}
