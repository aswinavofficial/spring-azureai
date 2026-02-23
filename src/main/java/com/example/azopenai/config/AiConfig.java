package com.example.azopenai.config;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI model configuration with per-model provider selection.
 *
 * <p>
 * Each model (chat, embedding) can independently use a different provider:
 * <ul>
 * <li>{@code azure} — Azure OpenAI (requires endpoint, api-key,
 * deployment-name)</li>
 * <li>{@code openai} — OpenAI-compatible API like Docker Model Runner (requires
 * base-url, model)</li>
 * </ul>
 *
 * <p>
 * Example: Azure chat + Docker Model Runner embedding, or vice versa.
 */
@Configuration
public class AiConfig {

    @Bean
    @Primary
    public ChatModel chatModel(AiProperties props) {
        var chat = props.getChat();

        return switch (chat.getProvider().toLowerCase()) {
            case "azure" -> buildAzureChatModel(chat);
            case "openai" -> buildOpenAiChatModel(chat);
            default -> throw new IllegalArgumentException(
                    "Unknown chat provider: " + chat.getProvider() + ". Use 'azure' or 'openai'.");
        };
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(AiProperties props) {
        var embedding = props.getEmbedding();

        return switch (embedding.getProvider().toLowerCase()) {
            case "azure" -> buildAzureEmbeddingModel(embedding);
            case "openai" -> buildOpenAiEmbeddingModel(embedding);
            default -> throw new IllegalArgumentException(
                    "Unknown embedding provider: " + embedding.getProvider() + ". Use 'azure' or 'openai'.");
        };
    }

    // ── Azure OpenAI ──────────────────────────────────────────

    private ChatModel buildAzureChatModel(AiProperties.ModelConfig cfg) {
        var clientBuilder = new OpenAIClientBuilder()
                .endpoint(cfg.getEndpoint())
                .credential(new AzureKeyCredential(cfg.getApiKey()));

        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(clientBuilder)
                .defaultOptions(AzureOpenAiChatOptions.builder()
                        .deploymentName(cfg.getDeploymentName())
                        .temperature(0.7)
                        .build())
                .build();
    }

    private EmbeddingModel buildAzureEmbeddingModel(AiProperties.ModelConfig cfg) {
        var openAiClient = new OpenAIClientBuilder()
                .endpoint(cfg.getEndpoint())
                .credential(new AzureKeyCredential(cfg.getApiKey()))
                .buildClient();

        return new AzureOpenAiEmbeddingModel(
                openAiClient,
                MetadataMode.EMBED,
                AzureOpenAiEmbeddingOptions.builder()
                        .deploymentName(cfg.getDeploymentName())
                        .build());
    }

    // ── OpenAI-compatible (Docker Model Runner, etc.) ─────────

    private ChatModel buildOpenAiChatModel(AiProperties.ModelConfig cfg) {
        var api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(cfg.getModel())
                        .temperature(0.7)
                        .build())
                .build();
    }

    private EmbeddingModel buildOpenAiEmbeddingModel(AiProperties.ModelConfig cfg) {
        var api = OpenAiApi.builder()
                .baseUrl(cfg.getBaseUrl())
                .apiKey(cfg.getApiKey())
                .build();

        return new OpenAiEmbeddingModel(
                api,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model(cfg.getModel())
                        .build());
    }
}
