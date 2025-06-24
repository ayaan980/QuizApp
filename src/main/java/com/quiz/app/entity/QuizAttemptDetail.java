package com.quiz.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "quiz_attempt_details")
@Data
public class QuizAttemptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    private String selectedAnswer;

    private String correctAnswer;

    private boolean isCorrect;

    private String subTopic; // Optional: Helps refine suggestions

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_result_id")
    @JsonIgnore
    private TestResults testResult;
}