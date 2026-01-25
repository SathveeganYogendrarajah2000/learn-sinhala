package com.learnsinhala.dto.practice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    @NotBlank(message = "Vocabulary ID is required")
    private String vocabularyId;

    @NotNull(message = "Result is required")
    private AnswerResult result;

    /**
     * How the user performed on this word:
     * - CORRECT: Knew the answer
     * - WRONG: Didn't know the answer
     * - SKIP: Skipped this word
     */
    public enum AnswerResult {
        CORRECT,
        WRONG,
        SKIP
    }
}
