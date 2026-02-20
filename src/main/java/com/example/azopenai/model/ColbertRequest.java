package com.example.azopenai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for ColBERT operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColbertRequest {

    /** Query text. */
    private String query;

    /** Document text to score against. */
    private String document;

    /** Text to encode into multi-vector representation. */
    private String text;
}
