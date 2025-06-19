package com.quiz.app.repo;

import com.quiz.app.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;



public interface QuestionRepository extends JpaRepository<Question, Long> {
}
