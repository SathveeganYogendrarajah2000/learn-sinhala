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
import com.learnsinhala.model.Role;
import com.learnsinhala.repository.UserProgressRepository;
import com.learnsinhala.repository.VocabularyRepository;
import com.learnsinhala.security.RoleValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.learnsinhala.dto.vocabulary.CsvUploadResponse;
import com.learnsinhala.dto.vocabulary.CsvUploadResponse.RowError;
import com.learnsinhala.dto.vocabulary.CsvVocabularyRow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
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
     * 
     * RBAC: Everyone can see all content (no filtering by creator)
     */
    public VocabularyListResponse listVocabulary(
            com.learnsinhala.model.User user,
            Category category,
            Difficulty difficulty,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("category", "difficulty"));

        // Everyone sees all content - no role-based filtering
        Page<Vocabulary> vocabularyPage = findAllVocabulary(category, difficulty, pageable);

        // Get user progress for all vocabulary items in this page
        List<String> vocabIds = vocabularyPage.getContent().stream()
                .map(Vocabulary::getId)
                .toList();

        Map<String, UserProgress> progressMap = userProgressRepository
                .findByUserIdAndVocabularyIdIn(user.getId(), vocabIds)
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
     * 
     * RBAC: Everyone can view all content
     */
    public VocabularyDto getVocabulary(com.learnsinhala.model.User user, String vocabularyId) {
        Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        // Everyone can view - no permission check needed

        UserProgress progress = userProgressRepository
                .findByUserIdAndVocabularyId(user.getId(), vocabularyId)
                .orElse(null);

        return VocabularyDto.from(vocab, toProgressInfo(progress));
    }

    /**
     * Create new vocabulary item.
     * RBAC: USER sets createdBy, ADMIN/SUPERADMIN leaves it null
     */
    public VocabularyDto createVocabulary(com.learnsinhala.model.User user, com.learnsinhala.dto.vocabulary.CreateVocabularyRequest request) {
        // USER: set createdBy, ADMIN/SUPERADMIN: leave it null
        String createdBy = (user.getRole() == Role.USER) ? user.getId() : null;
        
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
                .createdBy(createdBy)  // null for ADMIN+, userId for USER
                .build();

        vocab = vocabularyRepository.save(vocab);

        log.info("Created new vocabulary: {} ({}) by {} (role: {})", 
                vocab.getSinhala(), vocab.getId(), user.getEmail(), user.getRole());

        return VocabularyDto.from(vocab);
    }

    /**
     * Update existing vocabulary item.
     * Only updates fields that are provided (non-null).
     * 
     * RBAC: Only creator can update (USER updates own, ADMIN+ updates admin content)
     */
    public VocabularyDto updateVocabulary(com.learnsinhala.model.User user, String id, com.learnsinhala.dto.vocabulary.UpdateVocabularyRequest request) {
        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        // Check edit permission
        if (!RoleValidator.canEditContent(user, vocab.getCreatedBy())) {
            throw ApiException.forbidden("You don't have permission to edit this vocabulary");
        }

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

        log.info("Updated vocabulary: {} ({}) by user {}", vocab.getSinhala(), vocab.getId(), user.getEmail());

        return VocabularyDto.from(vocab);
    }

    /**
     * Delete vocabulary item and all associated user progress records.
     * 
     * RBAC: Only creator can delete (USER deletes own, ADMIN+ deletes admin content)
     */
    public void deleteVocabulary(com.learnsinhala.model.User user, String id) {
        // Verify vocabulary exists and get it
        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        // Check delete permission
        if (!RoleValidator.canDeleteContent(user, vocab.getCreatedBy())) {
            throw ApiException.forbidden("You don't have permission to delete this vocabulary");
        }

        // Delete all user progress for this vocabulary
        userProgressRepository.deleteByVocabularyId(id);

        // Delete the vocabulary
        vocabularyRepository.deleteById(id);

        log.info("Deleted vocabulary: {} by user {}", id, user.getEmail());
    }

    /**
     * Import vocabulary items from CSV file.
     *
     * Expected CSV format:
     * sinhala,pronunciation,tamil,english,category,difficulty,audioUrl,exampleSinhala,exampleEnglish,notes,tags
     *
     * - Required fields: sinhala, english, category, difficulty
     * - Optional fields: All others
     * - Tags: Semicolon-separated (e.g., "greetings;formal;common")
     *
     * @param user User importing (USER sets createdBy, ADMIN+ leaves null)
     * @param file CSV file uploaded by user
     * @return Upload results with success/error counts and details
     */
    @Transactional
    public CsvUploadResponse importFromCsv(com.learnsinhala.model.User user, MultipartFile file) {
        // USER: set createdBy, ADMIN/SUPERADMIN: leave it null
        String createdBy = (user.getRole() == Role.USER) ? user.getId() : null;
        
        log.info("Starting CSV import: {} by {} (role: {})", 
                file.getOriginalFilename(), user.getEmail(), user.getRole());

        List<Vocabulary> successfulRows = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();
        int rowNumber = 0;

        // Track sinhala words seen in this CSV to detect duplicates within the file
        Map<String, Integer> sinhalaSeen = new java.util.HashMap<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Configure CSV parser
            CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                    .setHeader()  // Use first row as header
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .setIgnoreEmptyLines(true)
                    .build();

            CSVParser csvParser = new CSVParser(reader, csvFormat);

            // Process each row
            for (CSVRecord record : csvParser) {
                rowNumber++;

                try {
                    // Parse CSV row
                    CsvVocabularyRow csvRow = parseCsvRecord(record);

                    // Validate required fields
                    List<String> validationErrors = csvRow.validate();
                    if (!validationErrors.isEmpty()) {
                        // Add all validation errors for this row
                        for (String error : validationErrors) {
                            errors.add(RowError.builder()
                                    .rowNumber(rowNumber)
                                    .message(error)
                                    .rawData(record.toString())
                                    .build());
                        }
                        continue;  // Skip this row
                    }

                    // Check for duplicate within CSV file
                    String sinhala = csvRow.getSinhala().trim().toLowerCase();
                    if (sinhalaSeen.containsKey(sinhala)) {
                        errors.add(RowError.builder()
                                .rowNumber(rowNumber)
                                .field("sinhala")
                                .message("Duplicate within CSV: '" + csvRow.getSinhala() + 
                                        "' already appears at row " + sinhalaSeen.get(sinhala))
                                .rawData(record.toString())
                                .build());
                        continue;  // Skip this row
                    }

                    // Check if vocabulary already exists in database
                    if (vocabularyRepository.existsBySinhala(csvRow.getSinhala())) {
                        errors.add(RowError.builder()
                                .rowNumber(rowNumber)
                                .field("sinhala")
                                .message("Duplicate entry: '" + csvRow.getSinhala() + 
                                        "' already exists in database")
                                .rawData(record.toString())
                                .build());
                        continue;  // Skip this row
                    }

                    // Mark this sinhala word as seen
                    sinhalaSeen.put(sinhala, rowNumber);

                    // Convert to Vocabulary entity
                    Vocabulary vocab = convertToVocabulary(createdBy, csvRow);
                    successfulRows.add(vocab);

                } catch (Exception e) {
                    log.warn("Error processing CSV row {}: {}", rowNumber, e.getMessage());
                    errors.add(RowError.builder()
                            .rowNumber(rowNumber)
                            .message("Parsing error: " + e.getMessage())
                            .rawData(record.toString())
                            .build());
                }
            }

            // Bulk insert successful rows
            List<String> createdIds = new ArrayList<>();
            if (!successfulRows.isEmpty()) {
                List<Vocabulary> savedVocabulary = vocabularyRepository.saveAll(successfulRows);
                createdIds = savedVocabulary.stream()
                        .map(Vocabulary::getId)
                        .toList();

                log.info("Successfully imported {} vocabulary items", createdIds.size());
            }

            // Build response
            int totalRows = rowNumber;
            int successCount = successfulRows.size();
            int errorCount = errors.size();

            log.info("CSV import completed: {} total rows, {} success, {} errors",
                    totalRows, successCount, errorCount);

            return CsvUploadResponse.builder()
                    .totalRows(totalRows)
                    .successCount(successCount)
                    .errorCount(errorCount)
                    .createdIds(createdIds)
                    .errors(errors)
                    .build();

        } catch (Exception e) {
            log.error("Fatal error during CSV import", e);
            throw ApiException.badRequest("Failed to process CSV file: " + e.getMessage());
        }
    }

    /**
     * Parse a CSV record into CsvVocabularyRow DTO
     */
    private CsvVocabularyRow parseCsvRecord(CSVRecord record) {
        // Parse tags (semicolon-separated)
        String tagsStr = getField(record, "tags");
        List<String> tags = new ArrayList<>();
        if (tagsStr != null && !tagsStr.trim().isEmpty()) {
            tags = Arrays.stream(tagsStr.split(";"))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .toList();
        }

        return CsvVocabularyRow.builder()
                .sinhala(getField(record, "sinhala"))
                .pronunciation(getField(record, "pronunciation"))
                .tamil(getField(record, "tamil"))
                .english(getField(record, "english"))
                .category(getField(record, "category"))
                .difficulty(getField(record, "difficulty"))
                .audioUrl(getField(record, "audioUrl"))
                .exampleSinhala(getField(record, "exampleSinhala"))
                .exampleEnglish(getField(record, "exampleEnglish"))
                .notes(getField(record, "notes"))
                .tags(tags)
                .build();
    }

    /**
     * Safely get field from CSV record, returning null if not present or empty
     */
    private String getField(CSVRecord record, String fieldName) {
        try {
            String value = record.get(fieldName);
            return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
        } catch (IllegalArgumentException e) {
            // Field not in CSV - that's okay for optional fields
            return null;
        }
    }

    /**
     * Convert CsvVocabularyRow to Vocabulary entity.
     * @param createdBy null for ADMIN+, userId for USER
     */
    private Vocabulary convertToVocabulary(String createdBy, CsvVocabularyRow csvRow) {
        return Vocabulary.builder()
                .sinhala(csvRow.getSinhala())
                .pronunciation(csvRow.getPronunciation())
                .tamil(csvRow.getTamil())
                .english(csvRow.getEnglish())
                .category(csvRow.getCategoryEnum())
                .difficulty(csvRow.getDifficultyEnum())
                .audioUrl(csvRow.getAudioUrl())
                .exampleSinhala(csvRow.getExampleSinhala())
                .exampleEnglish(csvRow.getExampleEnglish())
                .notes(csvRow.getNotes())
                .tags(csvRow.getTags())
                .createdBy(createdBy)  // null for ADMIN+, userId for USER
                .build();
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

    /**
     * Find vocabulary based on role and filters.
     * USER: Only their own content
     * ADMIN+: All content
     */
    private Page<Vocabulary> findVocabulary(com.learnsinhala.model.User user, Category category, Difficulty difficulty, Pageable pageable) {
        // Admin and above can see all content
        if (RoleValidator.isAdminOrAbove(user)) {
            return findAllVocabulary(category, difficulty, pageable);
        }
        
        // Regular users see only their own content
        String createdBy = user.getId();
        if (category != null && difficulty != null) {
            return vocabularyRepository.findByCategoryAndDifficultyAndCreatedBy(category, difficulty, createdBy, pageable);
        } else if (category != null) {
            return vocabularyRepository.findByCategoryAndCreatedBy(category, createdBy, pageable);
        } else if (difficulty != null) {
            return vocabularyRepository.findByDifficultyAndCreatedBy(difficulty, createdBy, pageable);
        } else {
            return vocabularyRepository.findByCreatedBy(createdBy, pageable);
        }
    }

    /**
     * Find all vocabulary without creator filtering (for ADMIN+).
     */
    private Page<Vocabulary> findAllVocabulary(Category category, Difficulty difficulty, Pageable pageable) {
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
