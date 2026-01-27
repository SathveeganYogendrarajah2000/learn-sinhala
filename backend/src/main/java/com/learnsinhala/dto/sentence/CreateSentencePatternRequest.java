package com.learnsinhala.dto.sentence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.SentencePattern.PatternExample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating new sentence pattern.
 * All Sinhala content must be in romanized English letters (no Sinhala script).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSentencePatternRequest {

    /**
     * Short name for the pattern (e.g., "Asking preferences")
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The sentence structure with placeholders (romanized Sinhala)
     * e.g., "{subject} {place}-ta yanna ona"
     */
    @NotBlank(message = "Sinhala pattern is required")
    private String sinhalaPattern;

    /**
     * Tamil equivalent pattern (for reference)
     */
    private String tamilPattern;

    /**
     * English equivalent pattern
     * e.g., "{subject} wants to go to {place}"
     */
    @NotBlank(message = "English pattern is required")
    private String englishPattern;

    /**
     * Explanation of when/how to use this pattern
     */
    private String usageNotes;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    /**
     * Concrete examples using this pattern
     */
    @Builder.Default
    private List<PatternExample> examples = new ArrayList<>();

    /**
     * Audio URL for the pattern (general pronunciation)
     */
    private String audioUrl;
}
