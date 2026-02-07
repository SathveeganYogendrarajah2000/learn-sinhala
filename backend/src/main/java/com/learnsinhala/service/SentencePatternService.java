package com.learnsinhala.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.learnsinhala.dto.sentence.CreateSentencePatternRequest;
import com.learnsinhala.dto.sentence.SentencePatternDto;
import com.learnsinhala.dto.sentence.UpdateSentencePatternRequest;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.model.SentencePattern;
import com.learnsinhala.model.Role;
import com.learnsinhala.repository.SentencePatternRepository;
import com.learnsinhala.security.RoleValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SentencePatternService {

    private final SentencePatternRepository sentencePatternRepository;

    /**
     * List sentence patterns with optional filters and pagination.
     * NEW RBAC: Everyone can view all content.
     */
    public List<SentencePatternDto> listSentencePatterns(
            com.learnsinhala.model.User user,
            Category category,
            Difficulty difficulty,
            Integer page,
            Integer size
    ) {
        // Everyone sees all content - no role-based filtering
        Page<SentencePattern> patternsPage;
        
        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("category", "difficulty"));
            patternsPage = findSentencePatterns(category, difficulty, pageable);
        } else {
            // Return all if no pagination
            List<SentencePattern> patterns = findAllSentencePatterns(category, difficulty);
            return patterns.stream()
                    .map(SentencePatternDto::from)
                    .collect(Collectors.toList());
        }

        return patternsPage.getContent().stream()
                .map(SentencePatternDto::from)
                .collect(Collectors.toList());
    }

    /**
     * Get a single sentence pattern by ID.
     * NEW RBAC: Everyone can view.
     */
    public SentencePatternDto getSentencePattern(com.learnsinhala.model.User user, String id) {
        SentencePattern pattern = sentencePatternRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sentence pattern not found"));

        // Everyone can view - no permission check needed
        return SentencePatternDto.from(pattern);
    }

    /**
     * Create new sentence pattern.
     * NEW RBAC: USER sets createdBy=userId, ADMIN/SUPERADMIN sets createdBy=null.
     */
    public SentencePatternDto createSentencePattern(com.learnsinhala.model.User user, CreateSentencePatternRequest request) {
        // Set createdBy based on role:
        // - USER: userId (for ownership tracking)
        // - ADMIN/SUPERADMIN: null (admin content)
        String createdBy = (user.getRole() == Role.USER) ? user.getId() : null;

        SentencePattern pattern = SentencePattern.builder()
                .name(request.getName())
                .sinhalaPattern(request.getSinhalaPattern())
                .tamilPattern(request.getTamilPattern())
                .englishPattern(request.getEnglishPattern())
                .usageNotes(request.getUsageNotes())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .examples(request.getExamples() != null ? request.getExamples() : new ArrayList<>())
                .audioUrl(request.getAudioUrl())
                .createdBy(createdBy)  // null for ADMIN+, userId for USER
                .build();

        pattern = sentencePatternRepository.save(pattern);

        log.info("Created new sentence pattern: {} ({}) by {}", pattern.getName(), pattern.getId(), 
                 createdBy != null ? createdBy : "admin");

        return SentencePatternDto.from(pattern);
    }

    /**
     * Update existing sentence pattern.
     * NEW RBAC: Only creator can edit.
     * Only updates fields that are provided (non-null).
     */
    public SentencePatternDto updateSentencePattern(com.learnsinhala.model.User user, String id, UpdateSentencePatternRequest request) {
        SentencePattern pattern = sentencePatternRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sentence pattern not found"));

        // Check edit permission: only creator can edit
        if (!RoleValidator.canEditContent(user, pattern.getCreatedBy())) {
            throw ApiException.forbidden("You don't have permission to edit this sentence pattern");
        }

        // Update only provided fields
        if (request.getName() != null) {
            pattern.setName(request.getName());
        }
        if (request.getSinhalaPattern() != null) {
            pattern.setSinhalaPattern(request.getSinhalaPattern());
        }
        if (request.getTamilPattern() != null) {
            pattern.setTamilPattern(request.getTamilPattern());
        }
        if (request.getEnglishPattern() != null) {
            pattern.setEnglishPattern(request.getEnglishPattern());
        }
        if (request.getUsageNotes() != null) {
            pattern.setUsageNotes(request.getUsageNotes());
        }
        if (request.getCategory() != null) {
            pattern.setCategory(request.getCategory());
        }
        if (request.getDifficulty() != null) {
            pattern.setDifficulty(request.getDifficulty());
        }
        if (request.getExamples() != null) {
            pattern.setExamples(request.getExamples());
        }
        if (request.getAudioUrl() != null) {
            pattern.setAudioUrl(request.getAudioUrl());
        }

        pattern = sentencePatternRepository.save(pattern);

        log.info("Updated sentence pattern: {} ({})", pattern.getName(), pattern.getId());

        return SentencePatternDto.from(pattern);
    }

    /**
     * Delete sentence pattern.
     * NEW RBAC: Only creator can delete.
     */
    public void deleteSentencePattern(com.learnsinhala.model.User user, String id) {
        SentencePattern pattern = sentencePatternRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Sentence pattern not found"));

        // Check delete permission: only creator can delete
        if (!RoleValidator.canDeleteContent(user, pattern.getCreatedBy())) {
            throw ApiException.forbidden("You don't have permission to delete this sentence pattern");
        }

        sentencePatternRepository.deleteById(id);

        log.info("Deleted sentence pattern: {}", id);
    }

    private Page<SentencePattern> findSentencePatterns(Category category, Difficulty difficulty, Pageable pageable) {
        if (category != null && difficulty != null) {
            return sentencePatternRepository.findByCategoryAndDifficulty(category, difficulty, pageable);
        } else if (category != null) {
            return sentencePatternRepository.findByCategory(category, pageable);
        } else if (difficulty != null) {
            return sentencePatternRepository.findByDifficulty(difficulty, pageable);
        } else {
            return sentencePatternRepository.findAll(pageable);
        }
    }

    private List<SentencePattern> findAllSentencePatterns(Category category, Difficulty difficulty) {
        if (category != null && difficulty != null) {
            return sentencePatternRepository.findByCategoryAndDifficulty(category, difficulty);
        } else if (category != null) {
            return sentencePatternRepository.findByCategory(category);
        } else if (difficulty != null) {
            return sentencePatternRepository.findByDifficulty(difficulty);
        } else {
            return sentencePatternRepository.findAll();
        }
    }
}
