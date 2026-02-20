package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for text and image summarization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummarizationRequest {

    /** Text to summarize (for text summarization). */
    private String text;

    /** Image URL to summarize (for image summarization). */
    private String imageUrl;

    /** Base64-encoded image data (for image summarization). */
    private String imageBase64;

    /** MIME type of the image (e.g., "image/jpeg", "image/png"). */
    private String imageMimeType;

    /** Optional custom summarization instruction. */
    private String customInstruction;
}
