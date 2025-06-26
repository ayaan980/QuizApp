package com.quiz.app.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.app.entity.QuizAttemptDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
                "max_tokens", 2048,  // increased to avoid cutoff
                "temperature", 0.7
        );

        String response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            System.out.println("\n=== Cohere RAW Response ===\n" + response);

            JsonNode root = objectMapper.readTree(response);
            String rawText = root.get("generations").get(0).get("text").asText();

            System.out.println("\n=== Extracted Text ===\n" + rawText);

            // Remove markdown if present
            rawText = rawText.replaceAll("(?i)```json", "")
                    .replaceAll("(?i)```", "")
                    .trim();

            // Validate bracket balance
            long openBrackets = rawText.chars().filter(ch -> ch == '[').count();
            long closeBrackets = rawText.chars().filter(ch -> ch == ']').count();
            if (openBrackets != closeBrackets) {
                throw new RuntimeException("❌ Unbalanced brackets in Cohere response. Probably truncated.\n" + rawText);
            }

            int startIndex = rawText.indexOf("[");
            int endIndex = rawText.lastIndexOf("]");
            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                throw new RuntimeException("❌ JSON array not found in Cohere response:\n" + rawText);
            }

            String jsonArray = rawText.substring(startIndex, endIndex + 1);
            System.out.println("\n=== Final JSON Array ===\n" + jsonArray);

            // Validate JSON structure
            JsonNode test = objectMapper.readTree(jsonArray);
            if (!test.isArray()) {
                throw new RuntimeException("Parsed content is not a JSON array.");
            }

            return jsonArray;

        } catch (Exception e) {
            System.err.println("❌ Error while parsing Cohere response:");
            e.printStackTrace();
            throw new RuntimeException("Error parsing JSON from Cohere response", e);
        }
    }
    public String generateImprovementSuggestions(List<QuizAttemptDetail> incorrectAnswers) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("A user has taken a quiz and answered the following questions incorrectly. ");
        promptBuilder.append("Analyze them and suggest which subtopics or concepts they should focus on to improve. ");
        promptBuilder.append("Return a short paragraph with concrete topics to study.\n\n");

        for (int i = 0; i < incorrectAnswers.size(); i++) {
            QuizAttemptDetail attempt = incorrectAnswers.get(i);
            promptBuilder.append("Q").append(i + 1).append(": ").append(attempt.getQuestion()).append("\n");
            promptBuilder.append("User's Answer: ").append(attempt.getSelectedAnswer()).append("\n");
            promptBuilder.append("Correct Answer: ").append(attempt.getCorrectAnswer()).append("\n\n");
        }

        String prompt = promptBuilder.toString();

        WebClient webClient = WebClient.builder()
                .baseUrl(cohereUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> request = Map.of(
                "model", "command-r-plus",
                "prompt", prompt,
                "max_tokens", 300,
                "temperature", 0.7
        );

        String response = webClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.get("generations").get(0).get("text").asText().trim();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Cohere response", e);
        }
    }
}