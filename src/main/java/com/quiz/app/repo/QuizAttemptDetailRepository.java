package com.quiz.app.repo;

import com.quiz.app.entity.QuizAttemptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizAttemptDetailRepository extends JpaRepository<QuizAttemptDetail, Long> {
    List<QuizAttemptDetail> findByTestResultId(Long testResultId);
}