package com.learnsinhala.dto.vocabulary;

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
public class VocabularyDto {

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
    private String notes;
    private List<String> tags;

    // User's progress on this vocabulary (null if not started)
    private ProgressInfo progress;

    public static VocabularyDto from(Vocabulary vocab) {
        return VocabularyDto.builder()
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
                .notes(vocab.getNotes())
                .tags(vocab.getTags())
                .build();
    }

    public static VocabularyDto from(Vocabulary vocab, ProgressInfo progress) {
        VocabularyDto dto = from(vocab);
        dto.setProgress(progress);
        return dto;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {
        private String status;
        private int correctCount;
        private int incorrectCount;
        private int streak;
        private double accuracy;
    }
}
