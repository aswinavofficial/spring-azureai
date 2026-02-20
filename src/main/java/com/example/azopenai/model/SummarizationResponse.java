package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for summarization operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationResponse {

    /** Generated summary text. */
    private String summary;

    /** Type of summarization performed: TEXT or IMAGE. */
    private String type;

    /** Processing time in milliseconds. */
    private long processingTimeMs;
}
