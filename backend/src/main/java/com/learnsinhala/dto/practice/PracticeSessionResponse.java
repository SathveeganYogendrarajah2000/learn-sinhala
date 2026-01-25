package com.learnsinhala.dto.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PracticeSessionResponse {

    private List<PracticeWord> words;
    private int totalWords;
    private int reviewWords;    // Words due for review
    private int newWords;       // New words introduced today
    private SessionStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStats {
        private long totalLearned;
        private long mastered;
        private long inProgress;
        private int dailyGoal;
        private int completedToday;
    }
}
