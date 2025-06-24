package com.quiz.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
@Data
public class TestResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private int totalQuestions;
    private int score;
    private LocalDateTime takenAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore  // ✅ Prevent circular reference during serialization
    private User user;

    // getters and setters
}