package com.example.azopenai.controller;

import com.example.azopenai.model.ColbertRequest;
import com.example.azopenai.model.ColbertResponse;
import com.example.azopenai.service.ColbertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for ColBERT late interaction operations.
 * Only active when app.colbert.enabled=true.
 */
@Slf4j
@RestController
@RequestMapping("/api/colbert")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.colbert.enabled", havingValue = "true")
public class ColbertController {

    private final ColbertService colbertService;

    /**
     * Encode text into multi-vector (per-token) representation.
     * POST /api/colbert/encode
     */
    @PostMapping("/encode")
    public ResponseEntity<Map<String, Object>> encodeTokens(@RequestBody ColbertRequest request) {
        log.info("ColBERT encode request received");
        long start = System.currentTimeMillis();

        List<float[]> tokenEmbeddings = colbertService.encodeTokens(request.getText());

        Map<String, Object> response = new HashMap<>();
        response.put("tokenCount", tokenEmbeddings.size());
        response.put("embeddingDimension", tokenEmbeddings.isEmpty() ? 0 : tokenEmbeddings.get(0).length);
        response.put("embeddings", tokenEmbeddings);
        response.put("processingTimeMs", System.currentTimeMillis() - start);

        return ResponseEntity.ok(response);
    }

    /**
     * Store a document with ColBERT multi-vector representation in Qdrant.
     * POST /api/colbert/store
     */
    @PostMapping("/store")
    public ResponseEntity<Map<String, Object>> storeDocument(@RequestBody ColbertRequest request) {
        log.info("ColBERT store request received");
        long start = System.currentTimeMillis();

        String docId = colbertService.storeDocument(request.getText(), null);

        Map<String, Object> response = new HashMap<>();
        response.put("documentId", docId);
        response.put("processingTimeMs", System.currentTimeMillis() - start);

        return ResponseEntity.ok(response);
    }

    /**
     * Compute MaxSim score between query and document.
     * POST /api/colbert/score
     */
    @PostMapping("/score")
    public ResponseEntity<ColbertResponse> score(@RequestBody ColbertRequest request) {
        log.info("ColBERT score request received");
        long start = System.currentTimeMillis();

        var result = colbertService.score(request.getQuery(), request.getDocument());

        var tokenScoreDetails = result.tokenScores().stream()
                .map(ts -> ColbertResponse.TokenScoreDetail.builder()
                        .queryToken(ts.queryToken())
                        .bestMatchDocToken(ts.bestMatchDocToken())
                        .similarity(ts.similarity())
                        .build())
                .toList();

        return ResponseEntity.ok(ColbertResponse.builder()
                .score(result.totalScore())
                .queryTokenCount(result.queryTokenCount())
                .documentTokenCount(result.documentTokenCount())
                .tokenScores(tokenScoreDetails)
                .processingTimeMs(System.currentTimeMillis() - start)
                .build());
    }
}
