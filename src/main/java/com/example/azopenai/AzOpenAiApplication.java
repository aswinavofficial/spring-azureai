package com.example.azopenai;

import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiImageAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiAudioTranscriptionAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        // Azure OpenAI auto-config (we configure manually)
        AzureOpenAiChatAutoConfiguration.class,
        AzureOpenAiEmbeddingAutoConfiguration.class,
        AzureOpenAiImageAutoConfiguration.class,
        AzureOpenAiAudioTranscriptionAutoConfiguration.class,
        // OpenAI auto-config (we configure manually)
        OpenAiChatAutoConfiguration.class,
        OpenAiEmbeddingAutoConfiguration.class,
        OpenAiImageAutoConfiguration.class,
        OpenAiAudioTranscriptionAutoConfiguration.class,
        OpenAiAudioSpeechAutoConfiguration.class,
        OpenAiModerationAutoConfiguration.class
})
public class AzOpenAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzOpenAiApplication.class, args);
    }
}
