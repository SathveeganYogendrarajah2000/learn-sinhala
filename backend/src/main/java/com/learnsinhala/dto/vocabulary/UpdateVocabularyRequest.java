package com.learnsinhala.dto.vocabulary;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating existing vocabulary.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVocabularyRequest {

    /**
     * Romanized Sinhala word/phrase
     */
    private String sinhala;

    /**
     * Pronunciation guide with syllable breaks
     */
    private String pronunciation;

    /**
     * Tamil translation
     */
    private String tamil;

    /**
     * English translation
     */
    private String english;

    private Category category;

    private Difficulty difficulty;

    /**
     * Relative path to audio file
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

    private List<String> tags;
}
