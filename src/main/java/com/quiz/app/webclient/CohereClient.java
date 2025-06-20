package com.quiz.app.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CohereClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.url}")
    private String cohereUrl;

    public String generateRawQuizJson(String topic) {
        String prompt = "Generate 5 quiz questions on " + topic +
                " with 3 options and the correct answer in this JSON format only (no explanation, no markdown, no text before or after): " +
                "[{\"question\": \"...\", \"options\": [\"...\", \"...\", \"...\"], \"answer\": \"...\"}]";

        WebClient webClient = WebClient.builder()
                .baseUrl(cohereUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> request = Map.of(
                "model", "command-r-plus",
                "prompt", prompt,
                "max_tokens", 500,
                "temperature", 0.7
        );

        String response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode node = objectMapper.readTree(response);
            String text = node.get("generations").get(0).get("text").asText();

            // Clean the response
            text = text
                    .replaceAll("(?i)```json", "")
                    .replaceAll("```", "")
                    .trim();

            // ✅ Extract JSON array using regex (robust against extra text)
            int start = text.indexOf("[");
            int end = text.lastIndexOf("]");
            if (start == -1 || end == -1) {
                throw new RuntimeException("JSON array not found in response");
            }

            String jsonArray = text.substring(start, end + 1);

            // Validate and return
            JsonNode quizArray = objectMapper.readTree(jsonArray);
            return quizArray.toString();

        } catch (Exception e) {
            throw new RuntimeException("❌ Error parsing Cohere response", e);
        }
    }
}