package com.learnsinhala.dto.vocabulary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for creating new vocabulary.
 * All Sinhala content must be in romanized English letters (no Sinhala script).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVocabularyRequest {

    /**
     * Romanized Sinhala word/phrase (e.g., "kohomada" for "how are you")
     */
    @NotBlank(message = "Sinhala is required")
    private String sinhala;

    /**
     * Pronunciation guide with syllable breaks (e.g., "ko-ho-ma-da")
     */
    private String pronunciation;

    /**
     * Tamil translation (helpful for Tamil speakers)
     */
    @NotBlank(message = "Tamil is required")
    private String tamil;

    /**
     * English translation
     */
    @NotBlank(message = "English is required")
    private String english;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    /**
     * Relative path to audio file (e.g., "/audio/greetings/kohomada.mp3")
     */
    private String audioUrl;

    /**
     * Example sentence using this word (romanized Sinhala)
     */
    private String exampleSinhala;

    /**
     * English translation of example sentence
     */
    private String exampleEnglish;

    /**
     * Usage notes or context tips
     */
    private String notes;

    @Builder.Default
    private List<String> tags = new ArrayList<>();
}
