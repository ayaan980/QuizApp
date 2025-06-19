package com.quiz.app.controller;

import com.quiz.app.entity.Question;
import com.quiz.app.service.QuizService;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@CrossOrigin(origins = "http://localhost:3000") // allow React dev server
public class QuizController {


    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/generate")
    public List<Question> generate(@RequestParam String topic) {
        return quizService.generateQuestion(topic);
    }
}
