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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configures two separate Azure OpenAI clients:
 * 1. Chat client (GPT-4o with vision) for summarization
 * 2. Embedding client (text-embedding-ada-002) for vector embeddings
 *
 * Each uses different Azure OpenAI endpoints and API keys.
 */
@Configuration
public class AzureOpenAiConfig {

        @Bean
        @Primary
        public ChatModel chatModel(AzureOpenAiProperties props) {
                var chatProps = props.getChat();
                var clientBuilder = new OpenAIClientBuilder()
                                .endpoint(chatProps.getEndpoint())
                                .credential(new AzureKeyCredential(chatProps.getApiKey()));

                return AzureOpenAiChatModel.builder()
                                .openAIClientBuilder(clientBuilder)
                                .defaultOptions(AzureOpenAiChatOptions.builder()
                                                .deploymentName(chatProps.getDeploymentName())
                                                .temperature(0.7)
                                                .build())
                                .build();
        }

        @Bean
        @Primary
        public EmbeddingModel embeddingModel(AzureOpenAiProperties props) {
                var embeddingProps = props.getEmbedding();
                var openAiClient = new OpenAIClientBuilder()
                                .endpoint(embeddingProps.getEndpoint())
                                .credential(new AzureKeyCredential(embeddingProps.getApiKey()))
                                .buildClient();

                return new AzureOpenAiEmbeddingModel(
                                openAiClient,
                                MetadataMode.EMBED,
                                AzureOpenAiEmbeddingOptions.builder()
                                                .deploymentName(embeddingProps.getDeploymentName())
                                                .build());
        }
}
