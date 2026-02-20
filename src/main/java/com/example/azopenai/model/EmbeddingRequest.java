package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for embedding operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {

    /** Text to embed. */
    private String text;

    /** Base64-encoded image to summarize → embed. */
    private String imageBase64;

    /** MIME type of the image. */
    private String imageMimeType;

    /** Image URL to summarize → embed. */
    private String imageUrl;

    /** Whether to store the embedding in Qdrant. */
    @Builder.Default
    private boolean store = false;

    /** Optional metadata to store alongside the embedding. */
    private Map<String, Object> metadata;
}
