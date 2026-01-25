package com.learnsinhala.dto.vocabulary;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProgressRequest {

    /**
     * The action to perform:
     * - KNOWN: Mark as mastered (increases interval significantly)
     * - LEARNING: Mark as still learning (moderate interval)
     * - HARD: Mark as difficult (decreases interval, needs more review)
     */
    @NotNull(message = "Action is required")
    private ProgressAction action;

    public enum ProgressAction {
        KNOWN,      // User knows this well
        LEARNING,   // User is still learning
        HARD        // User finds this difficult
    }
}
