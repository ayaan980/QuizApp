package com.quiz.app.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.app.entity.Question;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private final WebClient webClient;
    private  ObjectMapper objectMapper;

    public OpenAIClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer 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")
                .build();
    }

    public String generateRawQuizJson(String topic) {
        String prompt = "Generate 3 quiz questions on " + topic +
                " with 3 options and a correct answer in JSON array format like: " +
                "[{\"question\":\"...\",\"options\":[\"...\",\"...\",\"...\"],\"answer\":\"...\"}]";

        Map<String, Object> request = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 500
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}