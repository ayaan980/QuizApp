package com.quiz.app.service;

import com.quiz.app.entity.Question;
import com.quiz.app.repo.QuestionRepository;
import com.quiz.app.webclient.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository repository;
    private final OpenAIClient openAIClient;

    public List<Question> generateQuestion(String topic) {
        String rawResponse = openAIClient.generateRawQuizJson(topic);

        List<Question> dummy = List.of(
                new Question(null, "What is Java?",
                        List.of("A coffee", "A programming language", "A framework"),
                        "A programming language")
        );

        return repository.saveAll(dummy);
    }
}