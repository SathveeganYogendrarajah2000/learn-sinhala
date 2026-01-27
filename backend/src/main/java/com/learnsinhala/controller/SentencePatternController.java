package com.learnsinhala.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnsinhala.dto.sentence.CreateSentencePatternRequest;
import com.learnsinhala.dto.sentence.SentencePatternDto;
import com.learnsinhala.dto.sentence.UpdateSentencePatternRequest;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.CurrentUser;
import com.learnsinhala.service.SentencePatternService;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Sentence pattern management endpoints.
 *
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/sentence-patterns")
@RequiredArgsConstructor
public class SentencePatternController {

    private final SentencePatternService sentencePatternService;
    private final UserRepository userRepository;

    /**
     * List sentence patterns with optional filters.
     *
     * GET /api/sentence-patterns
     * GET /api/sentence-patterns?category=GREETINGS
     * GET /api/sentence-patterns?difficulty=BEGINNER
     */
    @GetMapping
    public ResponseEntity<List<SentencePatternDto>> listSentencePatterns(
            @CurrentUser UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        String userId = getUserId(userDetails);

        Category cat = parseCategory(category);
        Difficulty diff = parseDifficulty(difficulty);

        List<SentencePatternDto> patterns = sentencePatternService.listSentencePatterns(
                cat, diff, page, size
        );

        return ResponseEntity.ok(patterns);
    }

    /**
     * Get a single sentence pattern.
     *
     * GET /api/sentence-patterns/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SentencePatternDto> getSentencePattern(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = getUserId(userDetails);
        SentencePatternDto pattern = sentencePatternService.getSentencePattern(id);
        return ResponseEntity.ok(pattern);
    }

    /**
     * Create new sentence pattern.
     *
     * POST /api/sentence-patterns
     */
    @PostMapping
    public ResponseEntity<SentencePatternDto> createSentencePattern(
            @CurrentUser UserDetails userDetails,
            @Valid @RequestBody CreateSentencePatternRequest request
    ) {
        String userId = getUserId(userDetails);
        SentencePatternDto pattern = sentencePatternService.createSentencePattern(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pattern);
    }

    /**
     * Update existing sentence pattern.
     *
     * PUT /api/sentence-patterns/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<SentencePatternDto> updateSentencePattern(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody UpdateSentencePatternRequest request
    ) {
        String userId = getUserId(userDetails);
        SentencePatternDto pattern = sentencePatternService.updateSentencePattern(id, request);
        return ResponseEntity.ok(pattern);
    }

    /**
     * Delete sentence pattern.
     *
     * DELETE /api/sentence-patterns/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSentencePattern(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = getUserId(userDetails);
        sentencePatternService.deleteSentencePattern(id);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("User not found"))
                .getId();
    }

    private Category parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        try {
            return Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid category: " + category);
        }
    }

    private Difficulty parseDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return null;
        }
        try {
            return Difficulty.valueOf(difficulty.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Invalid difficulty: " + difficulty);
        }
    }
}
