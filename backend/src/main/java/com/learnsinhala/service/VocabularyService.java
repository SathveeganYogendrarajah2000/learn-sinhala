package com.learnsinhala.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.learnsinhala.dto.vocabulary.ProgressResponse;
import com.learnsinhala.dto.vocabulary.UpdateProgressRequest;
import com.learnsinhala.dto.vocabulary.UpdateProgressRequest.ProgressAction;
import com.learnsinhala.dto.vocabulary.VocabularyDto;
import com.learnsinhala.dto.vocabulary.VocabularyDto.ProgressInfo;
import com.learnsinhala.dto.vocabulary.VocabularyListResponse;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.LearningStatus;
import com.learnsinhala.model.UserProgress;
import com.learnsinhala.model.Vocabulary;
import com.learnsinhala.repository.UserProgressRepository;
import com.learnsinhala.repository.VocabularyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserProgressRepository userProgressRepository;

    /**
     * List vocabulary with optional filters and pagination.
     * Includes user's progress for each vocabulary item.
     */
    public VocabularyListResponse listVocabulary(
            String userId,
            Category category,
            Difficulty difficulty,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("category", "difficulty"));

        Page<Vocabulary> vocabularyPage = findVocabulary(category, difficulty, pageable);

        // Get user progress for all vocabulary items in this page
        List<String> vocabIds = vocabularyPage.getContent().stream()
                .map(Vocabulary::getId)
                .toList();

        Map<String, UserProgress> progressMap = userProgressRepository
                .findByUserIdAndVocabularyIdIn(userId, vocabIds)
                .stream()
                .collect(Collectors.toMap(UserProgress::getVocabularyId, Function.identity()));

        // Convert to DTOs with progress info
        List<VocabularyDto> items = vocabularyPage.getContent().stream()
                .map(vocab -> {
                    UserProgress progress = progressMap.get(vocab.getId());
                    return VocabularyDto.from(vocab, toProgressInfo(progress));
                })
                .toList();

        return VocabularyListResponse.of(
                items,
                page,
                size,
                vocabularyPage.getTotalElements()
        );
    }

    /**
     * Get a single vocabulary item with user's progress.
     */
    public VocabularyDto getVocabulary(String userId, String vocabularyId) {
        Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(userId, vocabularyId)
                .orElse(null);

        return VocabularyDto.from(vocab, toProgressInfo(progress));
    }

    /**
     * Create new vocabulary item.
     */
    public VocabularyDto createVocabulary(com.learnsinhala.dto.vocabulary.CreateVocabularyRequest request) {
        Vocabulary vocab = Vocabulary.builder()
                .sinhala(request.getSinhala())
                .pronunciation(request.getPronunciation())
                .tamil(request.getTamil())
                .english(request.getEnglish())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .audioUrl(request.getAudioUrl())
                .exampleSinhala(request.getExampleSinhala())
                .exampleEnglish(request.getExampleEnglish())
                .notes(request.getNotes())
                .tags(request.getTags() != null ? request.getTags() : new java.util.ArrayList<>())
                .build();

        vocab = vocabularyRepository.save(vocab);

        log.info("Created new vocabulary: {} ({})", vocab.getSinhala(), vocab.getId());

        return VocabularyDto.from(vocab);
    }

    /**
     * Update existing vocabulary item.
     * Only updates fields that are provided (non-null).
     */
    public VocabularyDto updateVocabulary(String id, com.learnsinhala.dto.vocabulary.UpdateVocabularyRequest request) {
        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        // Update only provided fields
        if (request.getSinhala() != null) {
            vocab.setSinhala(request.getSinhala());
        }
        if (request.getPronunciation() != null) {
            vocab.setPronunciation(request.getPronunciation());
        }
        if (request.getTamil() != null) {
            vocab.setTamil(request.getTamil());
        }
        if (request.getEnglish() != null) {
            vocab.setEnglish(request.getEnglish());
        }
        if (request.getCategory() != null) {
            vocab.setCategory(request.getCategory());
        }
        if (request.getDifficulty() != null) {
            vocab.setDifficulty(request.getDifficulty());
        }
        if (request.getAudioUrl() != null) {
            vocab.setAudioUrl(request.getAudioUrl());
        }
        if (request.getExampleSinhala() != null) {
            vocab.setExampleSinhala(request.getExampleSinhala());
        }
        if (request.getExampleEnglish() != null) {
            vocab.setExampleEnglish(request.getExampleEnglish());
        }
        if (request.getNotes() != null) {
            vocab.setNotes(request.getNotes());
        }
        if (request.getTags() != null) {
            vocab.setTags(request.getTags());
        }

        vocab = vocabularyRepository.save(vocab);

        log.info("Updated vocabulary: {} ({})", vocab.getSinhala(), vocab.getId());

        return VocabularyDto.from(vocab);
    }

    /**
     * Delete vocabulary item and all associated user progress records.
     */
    public void deleteVocabulary(String id) {
        // Verify vocabulary exists
        if (!vocabularyRepository.existsById(id)) {
            throw ApiException.notFound("Vocabulary not found");
        }

        // Delete all user progress for this vocabulary
        userProgressRepository.deleteByVocabularyId(id);

        // Delete the vocabulary
        vocabularyRepository.deleteById(id);

        log.info("Deleted vocabulary: {}", id);
    }

    /**
     * Update user's progress for a vocabulary item.
     * Implements simple spaced repetition algorithm.
     */
    public ProgressResponse updateProgress(
            String userId,
            String vocabularyId,
            UpdateProgressRequest request
    ) {
        // Verify vocabulary exists
        if (!vocabularyRepository.existsById(vocabularyId)) {
            throw ApiException.notFound("Vocabulary not found");
        }

        // Get or create progress record
        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(userId, vocabularyId)
                .orElseGet(() -> UserProgress.builder()
                        .userId(userId)
                        .vocabularyId(vocabularyId)
                        .build());

        // Update based on action
        String message = applyProgressAction(progress, request.getAction());

        progress.setLastPracticedAt(Instant.now());
        progress = userProgressRepository.save(progress);

        log.debug("Updated progress for user {} vocabulary {}: {}",
                userId, vocabularyId, progress.getStatus());

        return ProgressResponse.from(progress, message);
    }

    /**
     * Apply the progress action and return a message.
     * Implements spaced repetition intervals.
     */
    private String applyProgressAction(UserProgress progress, ProgressAction action) {
        Instant now = Instant.now();

        switch (action) {
            case KNOWN -> {
                // User knows this well - increase interval significantly
                progress.setCorrectCount(progress.getCorrectCount() + 1);
                progress.setStreak(progress.getStreak() + 1);

                // Increase interval: 4h -> 8h -> 24h -> 72h -> 168h (1 week)
                int newInterval = Math.min(progress.getIntervalHours() * 2, 168);
                progress.setIntervalHours(newInterval);
                progress.setNextReviewAt(now.plus(newInterval, ChronoUnit.HOURS));

                // Update status based on streak
                if (progress.getStreak() >= 5) {
                    progress.setStatus(LearningStatus.MASTERED);
                    return "Mastered! Next review in " + formatInterval(newInterval);
                } else if (progress.getStreak() >= 2) {
                    progress.setStatus(LearningStatus.REVIEWING);
                    return "Great! Next review in " + formatInterval(newInterval);
                } else {
                    progress.setStatus(LearningStatus.LEARNING);
                    return "Good! Next review in " + formatInterval(newInterval);
                }
            }
            case LEARNING -> {
                // Still learning - moderate interval
                progress.setCorrectCount(progress.getCorrectCount() + 1);
                progress.setStreak(progress.getStreak() + 1);
                progress.setStatus(LearningStatus.LEARNING);

                // Keep current interval or slight increase
                int newInterval = Math.min(progress.getIntervalHours() + 2, 24);
                progress.setIntervalHours(newInterval);
                progress.setNextReviewAt(now.plus(newInterval, ChronoUnit.HOURS));

                return "Keep practicing! Next review in " + formatInterval(newInterval);
            }
            case HARD -> {
                // User finds this difficult - reset interval
                progress.setIncorrectCount(progress.getIncorrectCount() + 1);
                progress.setStreak(0);
                progress.setStatus(LearningStatus.LEARNING);

                // Reset to short interval
                progress.setIntervalHours(2);
                progress.setNextReviewAt(now.plus(2, ChronoUnit.HOURS));

                return "No worries! We'll review again in 2 hours";
            }
            default -> throw ApiException.badRequest("Invalid action");
        }
    }

    private String formatInterval(int hours) {
        if (hours < 24) {
            return hours + " hours";
        } else if (hours < 168) {
            return (hours / 24) + " days";
        } else {
            return "1 week";
        }
    }

    private Page<Vocabulary> findVocabulary(Category category, Difficulty difficulty, Pageable pageable) {
        if (category != null && difficulty != null) {
            return vocabularyRepository.findByCategoryAndDifficulty(category, difficulty, pageable);
        } else if (category != null) {
            return vocabularyRepository.findByCategory(category, pageable);
        } else if (difficulty != null) {
            return vocabularyRepository.findByDifficulty(difficulty, pageable);
        } else {
            return vocabularyRepository.findAll(pageable);
        }
    }

    private ProgressInfo toProgressInfo(UserProgress progress) {
        if (progress == null) {
            return null;
        }
        return ProgressInfo.builder()
                .status(progress.getStatus().name())
                .correctCount(progress.getCorrectCount())
                .incorrectCount(progress.getIncorrectCount())
                .streak(progress.getStreak())
                .accuracy(progress.getAccuracy())
                .build();
    }
}
