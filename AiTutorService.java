package com.eduquest.service;

import com.eduquest.dto.AiTutorRequest;
import com.eduquest.dto.AiTutorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiTutorService {

    private final WebClient.Builder webClientBuilder;

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    @Value("${anthropic.api.url}")
    private String anthropicApiUrl;

    @Value("${anthropic.api.model}")
    private String model;

    @Value("${anthropic.api.max-tokens}")
    private int maxTokens;

    private static final String SYSTEM_PROMPT = """
        You are EduQuest AI Tutor, an expert tutor for college students in India.
        You specialize in Computer Science: Data Structures, Algorithms, DBMS, Operating Systems,
        Computer Networks, AI & Machine Learning, and Mathematics.
        
        Guidelines:
        - Keep explanations clear, accurate, and at college level
        - Use examples relevant to Indian engineering curriculum (GATE, competitive exams)
        - When explaining algorithms, include time/space complexity
        - Format code examples using proper code blocks
        - Use numbered lists for step-by-step explanations
        - End responses with an encouraging remark or a probing follow-up question
        - Maximum 250 words per response unless a detailed explanation is explicitly requested
        - Reference standard textbooks (CLRS, Tanenbaum, Silberschatz) when relevant
        """;

    public Mono<AiTutorResponse> askTutor(String userId, AiTutorRequest request) {
        log.info("AI tutor query from user {}: {}", userId, request.getMessage().substring(0, Math.min(50, request.getMessage().length())));

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", maxTokens,
            "system", SYSTEM_PROMPT,
            "messages", buildMessages(request)
        );

        return webClientBuilder.build()
            .post()
            .uri(anthropicApiUrl)
            .header("x-api-key", anthropicApiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Content-Type", "application/json")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::extractResponse)
            .onErrorReturn(new AiTutorResponse("I'm having trouble connecting right now. Please try again in a moment!", 0));
    }

    private List<Map<String, String>> buildMessages(AiTutorRequest request) {
        // Include conversation history for context
        var messages = new java.util.ArrayList<Map<String, String>>();
        if (request.getHistory() != null) {
            request.getHistory().forEach(h -> messages.add(Map.of("role", h.getRole(), "content", h.getContent())));
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));
        return messages;
    }

    @SuppressWarnings("unchecked")
    private AiTutorResponse extractResponse(Map<String, Object> response) {
        var content = (List<Map<String, Object>>) response.get("content");
        if (content == null || content.isEmpty()) return new AiTutorResponse("No response received.", 0);
        
        String text = (String) content.get(0).get("text");
        var usage = (Map<String, Object>) response.get("usage");
        int tokens = usage != null ? (int) usage.getOrDefault("output_tokens", 0) : 0;
        return new AiTutorResponse(text, tokens);
    }
}
