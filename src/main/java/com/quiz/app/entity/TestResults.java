package com.quiz.app.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
public class TestResults {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private int totalQuestions;
    private int score;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime takenAt = LocalDateTime.now();

    // Getters and Setters
}