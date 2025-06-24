package com.quiz.app.repo;


import com.quiz.app.entity.TestResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResults, Long> {
    List<TestResults> findByUserId(Long userId);
}
