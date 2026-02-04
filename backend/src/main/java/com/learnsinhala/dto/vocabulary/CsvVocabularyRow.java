package com.learnsinhala.dto.vocabulary;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal DTO representing a single CSV row for vocabulary import.
 * Used for parsing and validation before converting to Vocabulary entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvVocabularyRow {

    // Required fields
    private String sinhala;
    private String english;
    private String category;  // String for parsing, converted to enum
    private String difficulty;  // String for parsing, converted to enum

    // Optional fields
    private String pronunciation;
    private String tamil;
    private String audioUrl;
    private String exampleSinhala;
    private String exampleEnglish;
    private String notes;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Validates required fields are present and non-empty
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        if (sinhala == null || sinhala.trim().isEmpty()) {
            errors.add("sinhala is required");
        }
        if (english == null || english.trim().isEmpty()) {
            errors.add("english is required");
        }
        if (category == null || category.trim().isEmpty()) {
            errors.add("category is required");
        }
        if (difficulty == null || difficulty.trim().isEmpty()) {
            errors.add("difficulty is required");
        }

        // Validate category enum
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category.valueOf(category.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid category: " + category + ". Valid values: GREETINGS, NUMBERS, FOOD, TRAVEL, SHOPPING, FAMILY, WORK, TIME, WEATHER, DIRECTIONS, EMERGENCY, DAILY_PHRASES, QUESTIONS, RESPONSES");
            }
        }

        // Validate difficulty enum
        if (difficulty != null && !difficulty.trim().isEmpty()) {
            try {
                Difficulty.valueOf(difficulty.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid difficulty: " + difficulty + ". Valid values: BEGINNER, INTERMEDIATE, ADVANCED");
            }
        }

        return errors;
    }

    /**
     * Get Category enum (assumes validation has passed)
     */
    public Category getCategoryEnum() {
        if (category == null || category.trim().isEmpty()) {
            return null;
        }
        return Category.valueOf(category.toUpperCase().trim());
    }

    /**
     * Get Difficulty enum (assumes validation has passed)
     */
    public Difficulty getDifficultyEnum() {
        if (difficulty == null || difficulty.trim().isEmpty()) {
            return null;
        }
        return Difficulty.valueOf(difficulty.toUpperCase().trim());
    }
}
