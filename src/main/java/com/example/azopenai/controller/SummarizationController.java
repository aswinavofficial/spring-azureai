package com.example.azopenai.controller;

import com.example.azopenai.model.SummarizationRequest;
import com.example.azopenai.model.SummarizationResponse;
import com.example.azopenai.service.ImageSummarizationService;
import com.example.azopenai.service.TextSummarizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * REST controller for text and image summarization.
 */
@Slf4j
@RestController
@RequestMapping("/api/summarize")
@RequiredArgsConstructor
public class SummarizationController {

    private final TextSummarizationService textSummarizationService;
    private final ImageSummarizationService imageSummarizationService;

    /**
     * Summarize text.
     * POST /api/summarize/text
     */
    @PostMapping("/text")
    public ResponseEntity<SummarizationResponse> summarizeText(
            @RequestBody SummarizationRequest request) {
        log.info("Text summarization request received");
        long start = System.currentTimeMillis();

        String summary;
        if (request.getCustomInstruction() != null && !request.getCustomInstruction().isBlank()) {
            summary = textSummarizationService.summarize(request.getText(), request.getCustomInstruction());
        } else {
            summary = textSummarizationService.summarize(request.getText());
        }

        return ResponseEntity.ok(SummarizationResponse.builder()
                .summary(summary)
                .type("TEXT")
                .processingTimeMs(System.currentTimeMillis() - start)
                .build());
    }

    /**
     * Summarize an image (via URL, base64, or file upload).
     * POST /api/summarize/image
     */
    @PostMapping("/image")
    public ResponseEntity<SummarizationResponse> summarizeImage(
            @RequestBody SummarizationRequest request) {
        log.info("Image summarization request received");
        long start = System.currentTimeMillis();

        String summary;
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            summary = imageSummarizationService.summarizeImageFromUrl(request.getImageUrl());
        } else if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            byte[] imageBytes = Base64.getDecoder().decode(request.getImageBase64());
            String mimeType = request.getImageMimeType() != null ? request.getImageMimeType() : "image/jpeg";
            summary = imageSummarizationService.summarizeImage(imageBytes, mimeType);
        } else {
            return ResponseEntity.badRequest().body(SummarizationResponse.builder()
                    .summary("Error: Provide either imageUrl or imageBase64")
                    .type("ERROR")
                    .build());
        }

        return ResponseEntity.ok(SummarizationResponse.builder()
                .summary(summary)
                .type("IMAGE")
                .processingTimeMs(System.currentTimeMillis() - start)
                .build());
    }

    /**
     * Summarize an uploaded image file.
     * POST /api/summarize/image/upload
     */
    @PostMapping("/image/upload")
    public ResponseEntity<SummarizationResponse> summarizeImageUpload(
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Image upload summarization request received: {}", file.getOriginalFilename());
        long start = System.currentTimeMillis();

        String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String summary = imageSummarizationService.summarizeImage(file.getBytes(), mimeType);

        return ResponseEntity.ok(SummarizationResponse.builder()
                .summary(summary)
                .type("IMAGE")
                .processingTimeMs(System.currentTimeMillis() - start)
                .build());
    }
}
