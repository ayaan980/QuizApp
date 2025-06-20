package com.quiz.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.app.entity.Question;
import com.quiz.app.repo.QuestionRepository;
import com.quiz.app.webclient.CohereClient;
import com.quiz.app.webclient.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository repository;
    private final CohereClient cohereClient; // Not OpenAI anymore
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Question> generateQuestion(String topic) {
        String content = cohereClient.generateRawQuizJson(topic);
        List<Question> questions = new ArrayList<>();

        try {
            JsonNode parsed = objectMapper.readTree(content);
            if (parsed.isArray()) {
                for (JsonNode node : parsed) {
                    Question q = new Question(
                            null,
                            node.get("question").asText(),
                            objectMapper.convertValue(node.get("options"), List.class),
                            node.get("answer").asText()
                    );
                    questions.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return repository.saveAll(questions);
    }
}