package com.quiz.app.controller;

import com.quiz.app.entity.QuizAttemptDetail;
import com.quiz.app.entity.TestResults;
import com.quiz.app.entity.User;
import com.quiz.app.util.JwtUtil;
import com.quiz.app.repo.TestResultRepository;
import com.quiz.app.repo.UserRepository;
import com.quiz.app.webclient.CohereClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/results")
public class TestResultController {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CohereClient cohereClient;

    // ✅ GET all quiz results for logged-in user
    @GetMapping("/me")
    public List<TestResults> getUserResults(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return testResultRepository.findByUserId(user.getId());
    }

    // ✅ POST a new quiz result
    @PostMapping
    public ResponseEntity<?> saveTestResult(@RequestBody TestResults testResult, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        testResult.setUser(user);

        // Attach parent testResult to each attempt detail
        if (testResult.getAttemptDetails() != null) {
            for (QuizAttemptDetail detail : testResult.getAttemptDetails()) {
                detail.setTestResult(testResult);
            }
        }

        // Generate suggestion BEFORE saving, so it gets persisted
        String suggestion = "";
        List<QuizAttemptDetail> incorrect = testResult.getAttemptDetails()
                .stream()
                .filter(attempt ->
                        attempt.getSelectedAnswer() != null &&
                                attempt.getCorrectAnswer() != null &&
                                !attempt.getSelectedAnswer().equalsIgnoreCase(attempt.getCorrectAnswer())
                )
                .toList();

        if (!incorrect.isEmpty()) {
            suggestion = cohereClient.generateImprovementSuggestions(incorrect);
            testResult.setSuggestion(suggestion); // ✅ Save suggestion to DB
        }

        // Save the result and its attempt details
        TestResults savedResult = testResultRepository.save(testResult);

        // ✅ Return saved result + suggestion
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Result saved successfully");
        responseBody.put("suggestion", suggestion);
        responseBody.put("resultId", savedResult.getId());

        return ResponseEntity.ok(responseBody);
    }
}