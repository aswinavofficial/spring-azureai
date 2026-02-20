package com.example.azopenai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.List;

/**
 * Image summarization using Azure OpenAI GPT-4o with vision capabilities.
 * Sends images (base64 or URL) to GPT-4o and gets a textual
 * description/summary.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageSummarizationService {

    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
            You are an expert image analyst. Provide a detailed, structured summary of the image.
            Include: main subjects, actions, setting/background, text visible in the image,
            colors, mood/tone, and any other notable details. Be thorough but concise.
            This summary will be used for semantic search, so capture all meaningful content.
            """;

    private static final String USER_PROMPT = "Please analyze and summarize this image in detail:";

    /**
     * Summarize an image from raw bytes.
     */
    public String summarizeImage(byte[] imageBytes, String mimeType) {
        log.debug("Summarizing image of size: {} bytes, type: {}", imageBytes.length, mimeType);

        var media = Media.builder()
                .mimeType(MimeType.valueOf(mimeType))
                .data(imageBytes)
                .build();
        var userMessage = UserMessage.builder()
                .text(USER_PROMPT)
                .media(media)
                .build();
        var systemMessage = new SystemMessage(SYSTEM_PROMPT);
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        var response = chatModel.call(prompt);
        var summary = response.getResult().getOutput().getText();

        log.debug("Generated image summary of length: {}", summary.length());
        return summary;
    }

    /**
     * Summarize an image from a URL.
     */
    public String summarizeImageFromUrl(String imageUrl) {
        log.debug("Summarizing image from URL: {}", imageUrl);

        var media = Media.builder()
                .mimeType(MimeType.valueOf("image/jpeg"))
                .data(URI.create(imageUrl))
                .build();
        var userMessage = UserMessage.builder()
                .text(USER_PROMPT)
                .media(media)
                .build();
        var systemMessage = new SystemMessage(SYSTEM_PROMPT);
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        var response = chatModel.call(prompt);
        var summary = response.getResult().getOutput().getText();

        log.debug("Generated image summary of length: {}", summary.length());
        return summary;
    }
}
