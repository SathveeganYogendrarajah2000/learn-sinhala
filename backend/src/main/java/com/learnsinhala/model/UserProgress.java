package com.learnsinhala.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * UserProgress document - tracks learning progress per vocabulary item per user.
 *
 * Indexes:
 * - userId + vocabularyId (unique compound) - ensures one progress record per user-word pair
 * - userId + status - for fetching words by learning status
 * - userId + nextReviewAt - for spaced repetition queries
 *
 * Uses simple spaced repetition: correct answers increase interval, wrong answers reset it.
 */
@Document(collection = "user_progress")
@CompoundIndexes({
    @CompoundIndex(name = "user_vocab_idx", def = "{'userId': 1, 'vocabularyId': 1}", unique = true),
    @CompoundIndex(name = "user_status_idx", def = "{'userId': 1, 'status': 1}"),
    @CompoundIndex(name = "user_review_idx", def = "{'userId': 1, 'nextReviewAt': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {

    @Id
    private String id;

    private String userId;

    private String vocabularyId;

    @Builder.Default
    private LearningStatus status = LearningStatus.NEW;

    @Builder.Default
    private int correctCount = 0;

    @Builder.Default
    private int incorrectCount = 0;

    /**
     * Consecutive correct answers (resets on wrong answer)
     */
    @Builder.Default
    private int streak = 0;

    /**
     * Current interval in hours for spaced repetition
     */
    @Builder.Default
    private int intervalHours = 4;

    private Instant lastPracticedAt;

    /**
     * When this word should be reviewed next (for spaced repetition)
     */
    private Instant nextReviewAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Calculate accuracy percentage
     */
    public double getAccuracy() {
        int total = correctCount + incorrectCount;
        return total == 0 ? 0.0 : (double) correctCount / total * 100;
    }
}
