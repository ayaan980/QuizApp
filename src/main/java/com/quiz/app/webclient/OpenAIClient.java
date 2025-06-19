package com.quiz.app.webclient;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class OpenAIClient {

    private final WebClient webClient;

    public OpenAIClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer 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")
                .build();
    }

    public String generateRawQuizJson(String topic) {
        String prompt = "Generate 3 quiz questions on " + topic + " with options and correct answer in JSON format.";

        Map<String, Object> request = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 500
        );

        try {
            return webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .toEntity(String.class)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(ex -> ex instanceof WebClientResponseException.TooManyRequests)
                                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                            new RuntimeException("Rate limit exceeded after retries"))
                    )
                    .doOnSuccess(response -> {
                        String remaining = response.getHeaders().getFirst("x-ratelimit-remaining-tokens");
                        if (remaining != null) {
                            System.out.println("Remaining OpenAI tokens: " + remaining);
                        }
                    })
                    .map(response -> response.getBody())
                    .block();

        } catch (WebClientResponseException.TooManyRequests e) {
            System.err.println("❌ OpenAI Rate Limit Hit (429): " + e.getMessage());
            return "{\"error\": \"Too many requests. Please try again later.\"}";
        } catch (Exception e) {
            System.err.println("❌ OpenAI Error: " + e.getMessage());
            return "{\"error\": \"Unable to generate quiz. Please try again.\"}";
        }
    }
}