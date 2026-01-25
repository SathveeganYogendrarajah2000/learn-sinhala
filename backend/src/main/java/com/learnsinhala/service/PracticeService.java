package com.learnsinhala.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.learnsinhala.dto.practice.AnswerRequest;
import com.learnsinhala.dto.practice.AnswerRequest.AnswerResult;
import com.learnsinhala.dto.practice.AnswerResponse;
import com.learnsinhala.dto.practice.PracticeSessionResponse;
import com.learnsinhala.dto.practice.PracticeSessionResponse.SessionStats;
import com.learnsinhala.dto.practice.PracticeWord;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.LearningStatus;
import com.learnsinhala.model.User;
import com.learnsinhala.model.UserProgress;
import com.learnsinhala.model.Vocabulary;
import com.learnsinhala.repository.UserProgressRepository;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.repository.VocabularyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Handles practice session logic with simple spaced repetition.
 *
 * Spaced Repetition Algorithm:
 * - CORRECT: Double the interval (4h → 8h → 16h → 32h → 64h → 128h max)
 * - WRONG: Reset interval to 1 hour
 * - SKIP: No change, word appears again in current session
 *
 * Session composition:
 * 1. Words due for review (nextReviewAt <= now)
 * 2. New words to meet daily goal
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PracticeService {

    private final VocabularyRepository vocabularyRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserRepository userRepository;

    // Spaced repetition intervals in hours
    private static final int INITIAL_INTERVAL = 4;
    private static final int MIN_INTERVAL = 1;
    private static final int MAX_INTERVAL = 128; // ~5 days

    /**
     * Get today's practice words for a user.
     *
     * Returns:
     * 1. All words due for review
     * 2. New words up to daily goal
     */
    public PracticeSessionResponse getTodaysPractice(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        int dailyGoal = user.getPreferences() != null
                ? user.getPreferences().getDailyGoal()
                : 10;

        Instant now = Instant.now();

        // 1. Get words due for review
        List<UserProgress> dueForReview = userProgressRepository
                .findByUserIdAndNextReviewAtBefore(userId, now);

        Set<String> dueVocabIds = dueForReview.stream()
                .map(UserProgress::getVocabularyId)
                .collect(Collectors.toSet());

        // 2. Get user's all progress to find new words
        List<UserProgress> allProgress = userProgressRepository.findByUserId(userId);
        Set<String> seenVocabIds = allProgress.stream()
                .map(UserProgress::getVocabularyId)
                .collect(Collectors.toSet());

        // 3. Find new words (not seen before)
        int newWordsNeeded = Math.max(0, dailyGoal - dueForReview.size());
        List<Vocabulary> newVocabulary = findNewWords(seenVocabIds, newWordsNeeded);

        // 4. Fetch vocabulary for due reviews
        List<Vocabulary> reviewVocabulary = dueVocabIds.isEmpty()
                ? List.of()
                : vocabularyRepository.findAllById(dueVocabIds);

        // 5. Build progress map for lookup
        Map<String, UserProgress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(UserProgress::getVocabularyId, Function.identity()));

        // 6. Combine into practice words
        List<PracticeWord> practiceWords = new ArrayList<>();

        // Add review words first (higher priority)
        for (Vocabulary vocab : reviewVocabulary) {
            UserProgress progress = progressMap.get(vocab.getId());
            practiceWords.add(PracticeWord.from(vocab, progress));
        }

        // Add new words
        for (Vocabulary vocab : newVocabulary) {
            practiceWords.add(PracticeWord.from(vocab, null));
        }

        // 7. Build stats
        SessionStats stats = buildStats(userId, dailyGoal, allProgress);

        log.debug("Practice session for user {}: {} review, {} new",
                userId, reviewVocabulary.size(), newVocabulary.size());

        return PracticeSessionResponse.builder()
                .words(practiceWords)
                .totalWords(practiceWords.size())
                .reviewWords(reviewVocabulary.size())
                .newWords(newVocabulary.size())
                .stats(stats)
                .build();
    }

    /**
     * Submit an answer and update progress.
     */
    public AnswerResponse submitAnswer(String userId, AnswerRequest request) {
        String vocabId = request.getVocabularyId();

        // Verify vocabulary exists
        if (!vocabularyRepository.existsById(vocabId)) {
            throw ApiException.notFound("Vocabulary not found");
        }

        // Get or create progress
        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(userId, vocabId)
                .orElseGet(() -> createNewProgress(userId, vocabId));

        // Apply result
        Instant now = Instant.now();
        applyAnswer(progress, request.getResult(), now);

        // Save
        progress = userProgressRepository.save(progress);

        String feedback = AnswerResponse.getFeedback(request.getResult(), progress.getStreak());

        log.debug("Answer submitted: user={}, vocab={}, result={}, newStatus={}",
                userId, vocabId, request.getResult(), progress.getStatus());

        return AnswerResponse.builder()
                .vocabularyId(vocabId)
                .newStatus(progress.getStatus().name())
                .newStreak(progress.getStreak())
                .correctCount(progress.getCorrectCount())
                .incorrectCount(progress.getIncorrectCount())
                .accuracy(progress.getAccuracy())
                .nextReviewAt(progress.getNextReviewAt())
                .feedback(feedback)
                .build();
    }

    /**
     * Apply answer result to progress using spaced repetition.
     */
    private void applyAnswer(UserProgress progress, AnswerResult result, Instant now) {
        progress.setLastPracticedAt(now);

        switch (result) {
            case CORRECT -> {
                progress.setCorrectCount(progress.getCorrectCount() + 1);
                progress.setStreak(progress.getStreak() + 1);

                // Double the interval (with max cap)
                int newInterval = Math.min(progress.getIntervalHours() * 2, MAX_INTERVAL);
                progress.setIntervalHours(newInterval);
                progress.setNextReviewAt(now.plus(newInterval, ChronoUnit.HOURS));

                // Update status based on streak
                updateStatus(progress);
            }
            case WRONG -> {
                progress.setIncorrectCount(progress.getIncorrectCount() + 1);
                progress.setStreak(0);

                // Reset to minimum interval
                progress.setIntervalHours(MIN_INTERVAL);
                progress.setNextReviewAt(now.plus(MIN_INTERVAL, ChronoUnit.HOURS));

                // Back to learning
                progress.setStatus(LearningStatus.LEARNING);
            }
            case SKIP -> {
                // No changes to counts or interval
                // Word will appear again since we don't update nextReviewAt
            }
        }
    }

    /**
     * Update learning status based on streak.
     */
    private void updateStatus(UserProgress progress) {
        int streak = progress.getStreak();

        if (streak >= 5) {
            progress.setStatus(LearningStatus.MASTERED);
        } else if (streak >= 3) {
            progress.setStatus(LearningStatus.REVIEWING);
        } else {
            progress.setStatus(LearningStatus.LEARNING);
        }
    }

    /**
     * Create new progress record for a word.
     */
    private UserProgress createNewProgress(String userId, String vocabId) {
        return UserProgress.builder()
                .userId(userId)
                .vocabularyId(vocabId)
                .status(LearningStatus.NEW)
                .intervalHours(INITIAL_INTERVAL)
                .build();
    }

    /**
     * Find new words that user hasn't seen.
     */
    private List<Vocabulary> findNewWords(Set<String> seenVocabIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        // Get all vocabulary and filter out seen ones
        // For larger datasets, use a more efficient query
        List<Vocabulary> allVocab = vocabularyRepository.findAll(
                PageRequest.of(0, seenVocabIds.size() + limit)
        ).getContent();

        return allVocab.stream()
                .filter(v -> !seenVocabIds.contains(v.getId()))
                .limit(limit)
                .toList();
    }

    /**
     * Build session statistics.
     */
    private SessionStats buildStats(String userId, int dailyGoal, List<UserProgress> allProgress) {
        long mastered = allProgress.stream()
                .filter(p -> p.getStatus() == LearningStatus.MASTERED)
                .count();

        long inProgress = allProgress.stream()
                .filter(p -> p.getStatus() == LearningStatus.LEARNING ||
                             p.getStatus() == LearningStatus.REVIEWING)
                .count();

        // Count words practiced today
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        int completedToday = (int) allProgress.stream()
                .filter(p -> p.getLastPracticedAt() != null &&
                             p.getLastPracticedAt().isAfter(startOfDay))
                .count();

        return SessionStats.builder()
                .totalLearned(allProgress.size())
                .mastered(mastered)
                .inProgress(inProgress)
                .dailyGoal(dailyGoal)
                .completedToday(completedToday)
                .build();
    }
}
