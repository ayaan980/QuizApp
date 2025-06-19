package com.quiz.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.app.entity.Question;
import com.quiz.app.repo.QuestionRepository;
import com.quiz.app.webclient.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository repository;
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Question> generateQuestion(String topic) {
        List<Question> questions = new ArrayList<>();
        try {
            String rawResponse = openAIClient.generateRawQuizJson(topic);

            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choicesNode = root.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String content = choicesNode.get(0).path("message").path("content").asText();

                JsonNode parsed = objectMapper.readTree(content);
                if (parsed.isArray()) {
                    for (JsonNode node : parsed) {
                        String questionText = node.path("question").asText();
                        List<String> options = objectMapper.convertValue(node.path("options"), new TypeReference<List<String>>() {});
                        String answer = node.path("answer").asText();

                        if (!questionText.isEmpty() && !options.isEmpty() && !answer.isEmpty()) {
                            questions.add(new Question(null, questionText, options, answer));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing OpenAI response: " + e.getMessage());
            e.printStackTrace();
        }

        return repository.saveAll(questions);
    }
}