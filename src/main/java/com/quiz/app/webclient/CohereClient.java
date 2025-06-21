package com.quiz.app.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CohereClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cohere.api.key}")
    private String apiKey;

    @Value("${cohere.api.url}")
    private String cohereUrl;

    public String generateRawQuizJson(String topic, int count, String difficulty, int experience) {
        String prompt = String.format(
                "Generate %d %s-level quiz questions on '%s' for someone with %d years of experience. " +
                        "Each question should have 3 options and a correct answer, in this JSON format (no extra text): " +
                        "[{\"question\": \"...\", \"options\": [\"...\", \"...\", \"...\"], \"answer\": \"...\"}]",
                count, difficulty, topic, experience
        );

        WebClient webClient = WebClient.builder()
                .baseUrl(cohereUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> request = Map.of(
                "model", "command-r-plus",
                "prompt", prompt,
                "max_tokens", 1000,
                "temperature", 0.7
        );

        String response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(response);
            String rawText = root.get("generations").get(0).get("text").asText();

            // Clean markdown, trim
            rawText = rawText.replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();

            // Extract only the JSON array from response
            int startIndex = rawText.indexOf("[");
            int endIndex = rawText.lastIndexOf("]");

            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                throw new RuntimeException("❌ JSON array not found in Cohere response:\n" + rawText);
            }

            String jsonArray = rawText.substring(startIndex, endIndex + 1);

            // Validate JSON
            try {
                JsonNode test = objectMapper.readTree(jsonArray);
                if (!test.isArray()) {
                    throw new RuntimeException("Parsed content is not a JSON array.");
                }
            } catch (Exception ex) {
                throw new RuntimeException("❌ JSON validation failed: Probably incomplete or corrupt.\n\nResponse:\n" + jsonArray, ex);
            }

            return jsonArray;

        } catch (Exception e) {
            System.err.println("❌ Error while parsing Cohere response:");
            e.printStackTrace();
            throw new RuntimeException("Error parsing JSON from Cohere response", e);
        }
    }
}