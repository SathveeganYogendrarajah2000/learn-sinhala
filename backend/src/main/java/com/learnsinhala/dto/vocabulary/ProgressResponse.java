package com.learnsinhala.dto.vocabulary;

import com.learnsinhala.model.UserProgress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {

    private String vocabularyId;
    private String status;
    private int correctCount;
    private int incorrectCount;
    private int streak;
    private double accuracy;
    private Instant nextReviewAt;
    private String message;

    public static ProgressResponse from(UserProgress progress, String message) {
        return ProgressResponse.builder()
                .vocabularyId(progress.getVocabularyId())
                .status(progress.getStatus().name())
                .correctCount(progress.getCorrectCount())
                .incorrectCount(progress.getIncorrectCount())
                .streak(progress.getStreak())
                .accuracy(progress.getAccuracy())
                .nextReviewAt(progress.getNextReviewAt())
                .message(message)
                .build();
    }
}
