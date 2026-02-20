package com.example.azopenai;

import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiEmbeddingAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiImageAutoConfiguration;
import org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiAudioTranscriptionAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        AzureOpenAiChatAutoConfiguration.class,
        AzureOpenAiEmbeddingAutoConfiguration.class,
        AzureOpenAiImageAutoConfiguration.class,
        AzureOpenAiAudioTranscriptionAutoConfiguration.class
})
public class AzOpenAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AzOpenAiApplication.class, args);
    }
}
