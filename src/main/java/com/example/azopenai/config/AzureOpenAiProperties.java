package com.example.azopenai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Type-safe configuration properties for dual Azure OpenAI deployments.
 * Chat deployment uses GPT-4o (with vision), Embedding deployment uses
 * text-embedding model.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.azure")
public class AzureOpenAiProperties {

    private final DeploymentConfig chat = new DeploymentConfig();
    private final DeploymentConfig embedding = new DeploymentConfig();

    @Data
    public static class DeploymentConfig {
        private String endpoint;
        private String apiKey;
        private String deploymentName;
    }
}
