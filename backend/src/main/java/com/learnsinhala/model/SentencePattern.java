package com.learnsinhala.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * SentencePattern document - stores common sentence structures.
 *
 * Patterns use placeholders like {subject}, {object}, {verb} that can be filled
 * with vocabulary words to form complete sentences.
 *
 * Indexes:
 * - category - for category-based queries
 * - difficulty - for level-based queries
 *
 * Example pattern: "{subject} {object} kanna kamatida?" (Do you like to eat {object}?)
 */
@Document(collection = "sentence_patterns")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentencePattern {

    @Id
    private String id;

    /**
     * Short name for the pattern (e.g., "Asking preferences")
     */
    private String name;

    /**
     * The sentence structure with placeholders (romanized Sinhala)
     * e.g., "{subject} {place}-ta yanna ona"
     */
    private String sinhalaPattern;

    /**
     * Tamil equivalent pattern (for reference)
     */
    private String tamilPattern;

    /**
     * English equivalent pattern
     * e.g., "{subject} wants to go to {place}"
     */
    private String englishPattern;

    /**
     * Explanation of when/how to use this pattern
     */
    private String usageNotes;

    @Indexed
    private Category category;

    @Indexed
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

    /**
     * User ID of the creator. Used for RBAC.
     * - USER role can only view/edit/delete their own content
     * - ADMIN and SUPERADMIN can view/edit/delete all content
     */
    @Indexed
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatternExample {
        /**
         * Romanized Sinhala sentence
         */
        private String sinhala;

        /**
         * English translation
         */
        private String english;

        /**
         * Audio URL for this specific example
         */
        private String audioUrl;
    }
}
