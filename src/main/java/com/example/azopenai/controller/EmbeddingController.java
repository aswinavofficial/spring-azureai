package com.example.azopenai.controller;

import com.example.azopenai.model.*;
import com.example.azopenai.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for embedding operations and Qdrant vector search.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    /**
     * Embed text (and optionally store in Qdrant).
     * POST /api/embed/text
     */
    @PostMapping("/embed/text")
    public ResponseEntity<EmbeddingResponse> embedText(@RequestBody EmbeddingRequest request) {
        log.info("Text embedding request received");
        long start = System.currentTimeMillis();

        if (request.isStore()) {
            Map<String, Object> metadata = request.getMetadata() != null
                    ? request.getMetadata()
                    : new HashMap<>();
            String docId = embeddingService.embedAndStore(request.getText(), metadata);

            return ResponseEntity.ok(EmbeddingResponse.builder()
                    .documentId(docId)
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build());
        } else {
            float[] embedding = embeddingService.embedText(request.getText());

            return ResponseEntity.ok(EmbeddingResponse.builder()
                    .embedding(embedding)
                    .dimensions(embedding.length)
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build());
        }
    }

    /**
     * Summarize image → embed the summary (and optionally store in Qdrant).
     * POST /api/embed/image
     */
    @PostMapping("/embed/image")
    public ResponseEntity<EmbeddingResponse> embedImage(@RequestBody EmbeddingRequest request) {
        log.info("Image embedding request received");
        long start = System.currentTimeMillis();

        Map<String, Object> metadata = request.getMetadata() != null
                ? request.getMetadata()
                : new HashMap<>();

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            var result = embeddingService.embedImageFromUrlAndStore(request.getImageUrl(), metadata);

            return ResponseEntity.ok(EmbeddingResponse.builder()
                    .documentId(result.documentId())
                    .sourceSummary(result.imageSummary())
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build());

        } else if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            byte[] imageBytes = Base64.getDecoder().decode(request.getImageBase64());
            String mimeType = request.getImageMimeType() != null ? request.getImageMimeType() : "image/jpeg";

            var result = embeddingService.embedImageAndStore(imageBytes, mimeType, metadata);

            return ResponseEntity.ok(EmbeddingResponse.builder()
                    .documentId(result.documentId())
                    .sourceSummary(result.imageSummary())
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build());

        } else {
            return ResponseEntity.badRequest().body(EmbeddingResponse.builder()
                    .sourceSummary("Error: Provide either imageUrl or imageBase64")
                    .build());
        }
    }

    /**
     * Similarity search in Qdrant.
     * POST /api/search
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        log.info("Similarity search request: query='{}', topK={}", request.getQuery(), request.getTopK());
        long start = System.currentTimeMillis();

        var documents = embeddingService.similaritySearch(request.getQuery(), request.getTopK());

        var results = documents.stream()
                .map(doc -> SearchResponse.SearchResult.builder()
                        .documentId(doc.getId())
                        .content(doc.getText())
                        .metadata(doc.getMetadata())
                        .build())
                .toList();

        return ResponseEntity.ok(SearchResponse.builder()
                .results(results)
                .totalResults(results.size())
                .processingTimeMs(System.currentTimeMillis() - start)
                .build());
    }
}
