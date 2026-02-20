package com.example.azopenai.service;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;

/**
 * ColBERT-style late interaction service using Qdrant's native multi-vector
 * support.
 *
 * <p>
 * ColBERT (Contextualized Late Interaction over BERT) generates per-token
 * embeddings
 * instead of a single document embedding. At query time, it uses MaxSim
 * scoring:
 * for each query token, find the max cosine similarity with any document token,
 * then sum all these max scores.
 * </p>
 *
 * <p>
 * This implementation:
 * 1. Tokenizes text into individual tokens
 * 2. Generates per-token embeddings via Azure OpenAI Embedding model
 * 3. Stores multi-vectors in Qdrant collection with MaxSim comparator
 * 4. Implements MaxSim scoring for query-document relevance
 * </p>
 *
 * <p>
 * Activated when {@code app.colbert.enabled=true}
 * </p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.colbert.enabled", havingValue = "true")
public class ColbertService {

    private final EmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;
    private final String collectionName;

    public ColbertService(
            EmbeddingModel embeddingModel,
            @Value("${spring.ai.vectorstore.qdrant.host:localhost}") String qdrantHost,
            @Value("${spring.ai.vectorstore.qdrant.port:6334}") int qdrantPort,
            @Value("${app.colbert.collection-name:colbert_vectors}") String collectionName) {
        this.embeddingModel = embeddingModel;
        this.collectionName = collectionName;
        this.qdrantClient = new QdrantClient(
                QdrantGrpcClient.newBuilder(qdrantHost, qdrantPort, false).build());
    }

    /**
     * Initialize the Qdrant collection for ColBERT multi-vectors if it doesn't
     * exist.
     */
    @PostConstruct
    public void initCollection() {
        try {
            var collections = qdrantClient.listCollectionsAsync().get();
            boolean exists = collections.stream()
                    .anyMatch(c -> c.equals(collectionName));

            if (!exists) {
                log.info("Creating ColBERT collection: {}", collectionName);
                float[] testEmbed = embeddingModel.embed("test");
                int dimension = testEmbed.length;

                qdrantClient.createCollectionAsync(
                        collectionName,
                        Collections.VectorParams.newBuilder()
                                .setSize(dimension)
                                .setDistance(Collections.Distance.Cosine)
                                .setMultivectorConfig(Collections.MultiVectorConfig.newBuilder()
                                        .setComparator(Collections.MultiVectorComparator.MaxSim)
                                        .build())
                                .build())
                        .get();
                log.info("ColBERT collection created with dimension: {}", dimension);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to initialize ColBERT collection", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tokenize text and generate per-token embeddings (multi-vector
     * representation).
     */
    public List<float[]> encodeTokens(String text) {
        log.debug("Encoding tokens for text: '{}'", text.substring(0, Math.min(50, text.length())));

        List<String> tokens = tokenize(text);
        log.debug("Tokenized into {} tokens", tokens.size());

        List<float[]> tokenEmbeddings = new ArrayList<>();
        for (String token : tokens) {
            float[] embedding = embeddingModel.embed(token);
            tokenEmbeddings.add(embedding);
        }

        return tokenEmbeddings;
    }

    /**
     * Store a document's multi-vector representation in Qdrant.
     */
    public String storeDocument(String text, Map<String, Object> metadata) {
        log.debug("Storing ColBERT document: '{}'", text.substring(0, Math.min(50, text.length())));

        List<float[]> tokenEmbeddings = encodeTokens(text);
        String docId = UUID.randomUUID().toString();

        try {
            // Build DenseVector for each token embedding
            List<Points.DenseVector> denseVectors = tokenEmbeddings.stream()
                    .map(emb -> {
                        List<Float> floatList = new ArrayList<>();
                        for (float f : emb)
                            floatList.add(f);
                        return Points.DenseVector.newBuilder()
                                .addAllData(floatList)
                                .build();
                    })
                    .toList();

            // Create MultiDenseVector from all token DenseVectors
            Points.MultiDenseVector multiDenseVector = Points.MultiDenseVector.newBuilder()
                    .addAllVectors(denseVectors)
                    .build();

            // Wrap in Vector with setMultiDense
            Points.Vector vector = Points.Vector.newBuilder()
                    .setMultiDense(multiDenseVector)
                    .build();

            // Build payload
            Map<String, io.qdrant.client.grpc.JsonWithInt.Value> payload = new HashMap<>();
            payload.put("text", value(text));
            payload.put("token_count", value(tokenEmbeddings.size()));
            if (metadata != null) {
                metadata.forEach((k, v) -> payload.put(k, value(v.toString())));
            }

            // Create point with multi-vector
            Points.PointStruct point = Points.PointStruct.newBuilder()
                    .setId(id(UUID.fromString(docId)))
                    .setVectors(Points.Vectors.newBuilder()
                            .setVector(vector)
                            .build())
                    .putAllPayload(payload)
                    .build();

            qdrantClient.upsertAsync(collectionName, List.of(point)).get();
            log.debug("Stored ColBERT document with id: {}, tokens: {}", docId, tokenEmbeddings.size());

        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to store ColBERT document", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to store ColBERT document", e);
        }

        return docId;
    }

    /**
     * Compute MaxSim score between a query and a document.
     * For each query token, finds max cosine similarity with any document token,
     * then sums all these max scores.
     */
    public ColbertScoreResult score(String query, String document) {
        log.debug("Computing ColBERT MaxSim score");

        List<float[]> queryEmbeddings = encodeTokens(query);
        List<float[]> docEmbeddings = encodeTokens(document);

        double totalScore = 0.0;
        List<TokenScore> tokenScores = new ArrayList<>();

        List<String> queryTokens = tokenize(query);
        List<String> docTokens = tokenize(document);

        for (int i = 0; i < queryEmbeddings.size(); i++) {
            float[] qEmb = queryEmbeddings.get(i);
            double maxSim = Double.NEGATIVE_INFINITY;
            int bestDocIdx = -1;

            for (int j = 0; j < docEmbeddings.size(); j++) {
                double sim = cosineSimilarity(qEmb, docEmbeddings.get(j));
                if (sim > maxSim) {
                    maxSim = sim;
                    bestDocIdx = j;
                }
            }

            totalScore += maxSim;
            tokenScores.add(new TokenScore(
                    queryTokens.get(i),
                    bestDocIdx >= 0 ? docTokens.get(bestDocIdx) : "",
                    maxSim));
        }

        return new ColbertScoreResult(
                totalScore,
                queryEmbeddings.size(),
                docEmbeddings.size(),
                tokenScores);
    }

    /** Simple tokenization: split on whitespace. */
    private List<String> tokenize(String text) {
        return Arrays.stream(text.split("\\s+"))
                .filter(t -> !t.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /** Cosine similarity between two vectors. */
    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0 : dotProduct / denominator;
    }

    /** Score result with token-level details. */
    public record ColbertScoreResult(
            double totalScore,
            int queryTokenCount,
            int documentTokenCount,
            List<TokenScore> tokenScores) {
    }

    public record TokenScore(
            String queryToken,
            String bestMatchDocToken,
            double similarity) {
    }
}
