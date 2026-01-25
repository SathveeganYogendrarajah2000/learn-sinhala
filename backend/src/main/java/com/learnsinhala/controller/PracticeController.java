package com.learnsinhala.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnsinhala.dto.practice.AnswerRequest;
import com.learnsinhala.dto.practice.AnswerResponse;
import com.learnsinhala.dto.practice.PracticeSessionResponse;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.CurrentUser;
import com.learnsinhala.service.PracticeService;

import lombok.RequiredArgsConstructor;

/**
 * Practice session endpoints.
 *
 * Flow:
 * 1. GET /api/practice/today - Fetch today's practice words
 * 2. User practices each word
 * 3. POST /api/practice/answer - Submit result for each word
 * 4. Repeat until session complete
 */
@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;
    private final UserRepository userRepository;

    /**
     * Get today's practice session.
     *
     * Returns words that are:
     * - Due for review (based on spaced repetition)
     * - New words to learn (up to daily goal)
     *
     * GET /api/practice/today
     *
     * Response:
     * {
     *   "words": [...],
     *   "totalWords": 15,
     *   "reviewWords": 10,
     *   "newWords": 5,
     *   "stats": {
     *     "totalLearned": 50,
     *     "mastered": 20,
     *     "inProgress": 30,
     *     "dailyGoal": 10,
     *     "completedToday": 5
     *   }
     * }
     */
    @GetMapping("/today")
    public ResponseEntity<PracticeSessionResponse> getTodaysPractice(
            @CurrentUser UserDetails userDetails
    ) {
        String userId = getUserId(userDetails);
        PracticeSessionResponse session = practiceService.getTodaysPractice(userId);
        return ResponseEntity.ok(session);
    }

    /**
     * Submit answer for a practice word.
     *
     * POST /api/practice/answer
     *
     * Request:
     * {
     *   "vocabularyId": "abc123",
     *   "result": "CORRECT" | "WRONG" | "SKIP"
     * }
     *
     * Response:
     * {
     *   "vocabularyId": "abc123",
     *   "newStatus": "REVIEWING",
     *   "newStreak": 3,
     *   "correctCount": 5,
     *   "incorrectCount": 1,
     *   "accuracy": 83.33,
     *   "nextReviewAt": "2024-01-15T18:00:00Z",
     *   "feedback": "You're on a roll!"
     * }
     */
    @PostMapping("/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @CurrentUser UserDetails userDetails,
            @Valid @RequestBody AnswerRequest request
    ) {
        String userId = getUserId(userDetails);
        AnswerResponse response = practiceService.submitAnswer(userId, request);
        return ResponseEntity.ok(response);
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("User not found"))
                .getId();
    }
}
