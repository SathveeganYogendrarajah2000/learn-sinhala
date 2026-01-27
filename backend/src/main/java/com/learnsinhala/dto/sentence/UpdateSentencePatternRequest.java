package com.learnsinhala.dto.sentence;

import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.SentencePattern.PatternExample;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating existing sentence pattern.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSentencePatternRequest {

    private String name;

    private String sinhalaPattern;

    private String tamilPattern;

    private String englishPattern;

    private String usageNotes;

    private Category category;

    private Difficulty difficulty;

    private List<PatternExample> examples;

    private String audioUrl;
}
