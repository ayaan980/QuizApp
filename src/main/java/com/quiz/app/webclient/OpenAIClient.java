package com.quiz.app.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.quiz.app.config.OpenAIConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final WebClient webClient;


    public OpenAIClient(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey ).build();
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
                .onStatus(status -> status.value() == 429, response -> {
                    System.out.println("⚠️ OpenAI rate limit hit (429). Retrying...");
                    return response.createException();
                })
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .block();
    }
}