package com.quiz.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime takenAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore // Prevent circular reference when serializing to JSON
    private User user;

    @OneToMany(mappedBy = "testResult", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<QuizAttemptDetail> attemptDetails;

    @PrePersist
    protected void onCreate() {
        this.takenAt = LocalDateTime.now();
    }

    @Column(columnDefinition = "TEXT")
    private String suggestion;
}