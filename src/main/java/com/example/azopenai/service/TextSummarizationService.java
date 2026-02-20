package com.example.azopenai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Text summarization using Azure OpenAI Chat model (GPT-4o).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextSummarizationService {

    private final ChatModel chatModel;

    private static final String SYSTEM_PROMPT = """
            You are an expert summarizer. Provide a clear, concise, and comprehensive summary
            of the given text. Maintain the key points, important details, and overall meaning.
            Keep the summary well-structured and easy to read.
            """;

    /**
     * Summarize the given text.
     */
    public String summarize(String text) {
        log.debug("Summarizing text of length: {}", text.length());

        var systemMessage = new SystemMessage(SYSTEM_PROMPT);
        var userMessage = new UserMessage("Please summarize the following text:\n\n" + text);
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        var response = chatModel.call(prompt);
        var summary = response.getResult().getOutput().getText();

        log.debug("Generated summary of length: {}", summary.length());
        return summary;
    }

    /**
     * Summarize text with a custom instruction.
     */
    public String summarize(String text, String customInstruction) {
        log.debug("Summarizing text with custom instruction");

        var systemMessage = new SystemMessage(customInstruction);
        var userMessage = new UserMessage(text);
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        var response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
