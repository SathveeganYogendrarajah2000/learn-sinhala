package com.learnsinhala.dto.sentence;

import com.learnsinhala.model.SentencePattern;
import com.learnsinhala.model.SentencePattern.PatternExample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for sentence pattern.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentencePatternDto {

    private String id;
    private String name;
    private String sinhalaPattern;
    private String tamilPattern;
    private String englishPattern;
    private String usageNotes;
    private String category;
    private String difficulty;
    private List<PatternExample> examples;
    private String audioUrl;

    public static SentencePatternDto from(SentencePattern pattern) {
        return SentencePatternDto.builder()
                .id(pattern.getId())
                .name(pattern.getName())
                .sinhalaPattern(pattern.getSinhalaPattern())
                .tamilPattern(pattern.getTamilPattern())
                .englishPattern(pattern.getEnglishPattern())
                .usageNotes(pattern.getUsageNotes())
                .category(pattern.getCategory() != null ? pattern.getCategory().name() : null)
                .difficulty(pattern.getDifficulty() != null ? pattern.getDifficulty().name() : null)
                .examples(pattern.getExamples())
                .audioUrl(pattern.getAudioUrl())
                .build();
    }
}
