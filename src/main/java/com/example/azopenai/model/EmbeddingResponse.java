package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for embedding operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {

    /** The embedding vector. */
    private float[] embedding;

    /** Dimension of the embedding. */
    private int dimensions;

    /** If image was embedded, the generated text summary used for embedding. */
    private String sourceSummary;

    /** Document ID if stored in Qdrant. */
    private String documentId;

    /** Processing time in milliseconds. */
    private long processingTimeMs;
}
