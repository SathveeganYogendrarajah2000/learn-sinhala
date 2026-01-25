package com.learnsinhala.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
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
 * Vocabulary document - stores words and phrases.
 *
 * Indexes:
 * - category + difficulty (compound) - for filtered queries
 * - tags - for tag-based searches
 * - sinhala (text) - for search functionality
 *
 * All Sinhala content is stored in romanized English letters (no Sinhala script).
 */
@Document(collection = "vocabulary")
@CompoundIndex(name = "category_difficulty_idx", def = "{'category': 1, 'difficulty': 1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    private String id;

    /**
     * Romanized Sinhala word/phrase (e.g., "kohomada" for "how are you")
     */
    private String sinhala;

    /**
     * Pronunciation guide with syllable breaks (e.g., "ko-ho-ma-da")
     */
    private String pronunciation;

    /**
     * Tamil translation (helpful for Tamil speakers)
     */
    private String tamil;

    /**
     * English translation
     */
    private String english;

    @Indexed
    private Category category;

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

    @Indexed
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
