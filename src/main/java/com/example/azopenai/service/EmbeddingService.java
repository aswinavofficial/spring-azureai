package com.example.azopenai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Embedding service using Azure OpenAI Embedding model + Qdrant vector store.
 * Supports direct text embedding and image-to-text-to-embedding pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;
    private final ImageSummarizationService imageSummarizationService;

    /**
     * Generate embedding for text.
     */
    public float[] embedText(String text) {
        log.debug("Generating embedding for text of length: {}", text.length());
        var response = embeddingModel.embed(text);
        return response;
    }

    /**
     * Embed text and store in Qdrant with metadata.
     */
    public String embedAndStore(String text, Map<String, Object> metadata) {
        log.debug("Embedding and storing text in Qdrant");
        var document = new Document(text, metadata);
        vectorStore.add(List.of(document));
        log.debug("Stored document with id: {}", document.getId());
        return document.getId();
    }

    /**
     * Summarize image first, then embed the summary and store in Qdrant.
     * This is the image-to-text-to-embedding pipeline.
     */
    public ImageEmbeddingResult embedImageAndStore(byte[] imageBytes, String mimeType,
            Map<String, Object> metadata) {
        log.debug("Starting image→summary→embedding pipeline");

        // Step 1: Summarize image using GPT-4o vision
        String imageSummary = imageSummarizationService.summarizeImage(imageBytes, mimeType);
        log.debug("Image summarized. Summary length: {}", imageSummary.length());

        // Step 2: Add source info to metadata
        var enrichedMetadata = new java.util.HashMap<>(metadata);
        enrichedMetadata.put("source_type", "image");
        enrichedMetadata.put("image_mime_type", mimeType);
        enrichedMetadata.put("image_size_bytes", imageBytes.length);
        enrichedMetadata.put("image_base64_preview", Base64.getEncoder()
                .encodeToString(imageBytes).substring(0, Math.min(100, imageBytes.length)));

        // Step 3: Embed the summary and store
        var document = new Document(imageSummary, enrichedMetadata);
        vectorStore.add(List.of(document));
        log.debug("Image embedding stored with id: {}", document.getId());

        return new ImageEmbeddingResult(document.getId(), imageSummary);
    }

    /**
     * Summarize image from URL, then embed and store.
     */
    public ImageEmbeddingResult embedImageFromUrlAndStore(String imageUrl,
            Map<String, Object> metadata) {
        log.debug("Starting image URL→summary→embedding pipeline");

        String imageSummary = imageSummarizationService.summarizeImageFromUrl(imageUrl);

        var enrichedMetadata = new java.util.HashMap<>(metadata);
        enrichedMetadata.put("source_type", "image");
        enrichedMetadata.put("image_url", imageUrl);

        var document = new Document(imageSummary, enrichedMetadata);
        vectorStore.add(List.of(document));

        return new ImageEmbeddingResult(document.getId(), imageSummary);
    }

    /**
     * Perform similarity search in Qdrant.
     */
    public List<Document> similaritySearch(String query, int topK) {
        log.debug("Performing similarity search for query: '{}', topK: {}", query, topK);
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .build());
    }

    /**
     * Result holder for image embedding operations.
     */
    public record ImageEmbeddingResult(String documentId, String imageSummary) {
    }
}
