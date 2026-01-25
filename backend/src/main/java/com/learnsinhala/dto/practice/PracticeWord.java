package com.learnsinhala.dto.practice;

import com.learnsinhala.model.UserProgress;
import com.learnsinhala.model.Vocabulary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeWord {

    private String id;
    private String sinhala;
    private String pronunciation;
    private String tamil;
    private String english;
    private String category;
    private String difficulty;
    private String audioUrl;
    private String exampleSinhala;
    private String exampleEnglish;
    private List<String> tags;

    // Practice metadata
    private boolean isNew;          // First time seeing this word
    private boolean isReview;       // Due for review
    private int currentStreak;
    private String currentStatus;

    public static PracticeWord from(Vocabulary vocab, UserProgress progress) {
        boolean isNew = progress == null;

        return PracticeWord.builder()
                .id(vocab.getId())
                .sinhala(vocab.getSinhala())
                .pronunciation(vocab.getPronunciation())
                .tamil(vocab.getTamil())
                .english(vocab.getEnglish())
                .category(vocab.getCategory() != null ? vocab.getCategory().name() : null)
                .difficulty(vocab.getDifficulty() != null ? vocab.getDifficulty().name() : null)
                .audioUrl(vocab.getAudioUrl())
                .exampleSinhala(vocab.getExampleSinhala())
                .exampleEnglish(vocab.getExampleEnglish())
                .tags(vocab.getTags())
                .isNew(isNew)
                .isReview(!isNew)
                .currentStreak(isNew ? 0 : progress.getStreak())
                .currentStatus(isNew ? "NEW" : progress.getStatus().name())
                .build();
    }
}
