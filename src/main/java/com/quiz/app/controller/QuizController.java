package com.quiz.app.controller;

import com.quiz.app.entity.Question;
import com.quiz.app.service.QuizService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "https://coruscating-peony-e44b25.netlify.app")
public class QuizController {


    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/generate")
    public List<Question> generate(
            @RequestParam String topic,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "easy") String difficulty,
            @RequestParam(defaultValue = "0") int experience
    ) {
        return quizService.generateQuestion(topic, count, difficulty, experience);
    }
}
