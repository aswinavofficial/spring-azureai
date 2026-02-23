package com.example.azopenai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Unified AI configuration with per-model provider selection.
 * Each model (chat, embedding) can independently use 'azure' or 'openai'.
 *
 * Example: chat on Azure OpenAI + embedding on Docker Model Runner.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private ModelConfig chat = new ModelConfig();
    private ModelConfig embedding = new ModelConfig();

    @Data
    public static class ModelConfig {
        /** Provider: "azure" or "openai" */
        private String provider = "azure";

        // --- Azure OpenAI fields ---
        private String endpoint;
        private String apiKey;
        private String deploymentName;

        // --- OpenAI-compatible fields ---
        private String baseUrl = "http://localhost:12434/engines/llama.cpp/v1";
        private String model;
    }
}
