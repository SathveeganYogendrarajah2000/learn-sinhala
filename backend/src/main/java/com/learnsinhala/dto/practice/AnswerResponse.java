package com.learnsinhala.dto.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    private String vocabularyId;
    private String newStatus;
    private int newStreak;
    private int correctCount;
    private int incorrectCount;
    private double accuracy;
    private Instant nextReviewAt;
    private String feedback;

    // Encouragement messages
    public static String getFeedback(AnswerRequest.AnswerResult result, int streak) {
        return switch (result) {
            case CORRECT -> switch (streak) {
                case 1 -> "Good start!";
                case 2 -> "Keep it up!";
                case 3 -> "You're on a roll!";
                case 4 -> "Excellent progress!";
                default -> streak >= 5 ? "Mastered!" : "Well done!";
            };
            case WRONG -> "Don't worry, practice makes perfect!";
            case SKIP -> "We'll come back to this one.";
        };
    }
}
