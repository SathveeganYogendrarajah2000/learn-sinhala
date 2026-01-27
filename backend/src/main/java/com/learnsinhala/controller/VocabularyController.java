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

import com.learnsinhala.dto.vocabulary.CreateVocabularyRequest;
import com.learnsinhala.dto.vocabulary.ProgressResponse;
import com.learnsinhala.dto.vocabulary.UpdateProgressRequest;
import com.learnsinhala.dto.vocabulary.UpdateVocabularyRequest;
import com.learnsinhala.dto.vocabulary.VocabularyDto;
import com.learnsinhala.dto.vocabulary.VocabularyListResponse;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Category;
import com.learnsinhala.model.Difficulty;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.CurrentUser;
import com.learnsinhala.service.VocabularyService;

import lombok.RequiredArgsConstructor;

/**
 * Vocabulary management endpoints.
 *
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;
    private final UserRepository userRepository;

    /**
     * List vocabulary with optional filters.
     *
     * Query params:
     * - category: GREETINGS, NUMBERS, FOOD, etc. (optional)
     * - difficulty: BEGINNER, INTERMEDIATE, ADVANCED (optional)
     * - page: Page number, 0-indexed (default: 0)
     * - size: Items per page (default: 20)
     *
     * GET /api/vocabulary
     * GET /api/vocabulary?category=GREETINGS
     * GET /api/vocabulary?difficulty=BEGINNER
     * GET /api/vocabulary?category=FOOD&difficulty=BEGINNER&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<VocabularyListResponse> listVocabulary(
            @CurrentUser UserDetails userDetails,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = getUserId(userDetails);

        Category cat = parseCategory(category);
        Difficulty diff = parseDifficulty(difficulty);

        VocabularyListResponse response = vocabularyService.listVocabulary(
                userId, cat, diff, page, size
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get a single vocabulary item.
     *
     * GET /api/vocabulary/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<VocabularyDto> getVocabulary(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = getUserId(userDetails);
        VocabularyDto vocab = vocabularyService.getVocabulary(userId, id);
        return ResponseEntity.ok(vocab);
    }

    /**
     * Create new vocabulary item.
     *
     * POST /api/vocabulary
     *
     * Request body:
     * {
     *   "sinhala": "ayubowan",
     *   "tamil": "வணக்கம்",
     *   "english": "hello",
     *   "category": "GREETINGS",
     *   "difficulty": "BEGINNER",
     *   "audioUrl": "/audio/greetings/ayubowan.mp3" (optional)
     * }
     */
    @PostMapping
    public ResponseEntity<VocabularyDto> createVocabulary(
            @CurrentUser UserDetails userDetails,
            @Valid @RequestBody CreateVocabularyRequest request
    ) {
        String userId = getUserId(userDetails);
        VocabularyDto vocab = vocabularyService.createVocabulary(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(vocab);
    }

    /**
     * Update existing vocabulary item.
     *
     * PUT /api/vocabulary/{id}
     *
     * Request body (all fields optional):
     * {
     *   "english": "greeting",
     *   "notes": "Formal greeting"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<VocabularyDto> updateVocabulary(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody UpdateVocabularyRequest request
    ) {
        String userId = getUserId(userDetails);
        VocabularyDto vocab = vocabularyService.updateVocabulary(id, request);
        return ResponseEntity.ok(vocab);
    }

    /**
     * Delete vocabulary item.
     *
     * DELETE /api/vocabulary/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVocabulary(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = getUserId(userDetails);
        vocabularyService.deleteVocabulary(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update learning progress for a vocabulary item.
     *
     * POST /api/vocabulary/{id}/progress
     *
     * Request body:
     * {
     *   "action": "KNOWN" | "LEARNING" | "HARD"
     * }
     *
     * Actions:
     * - KNOWN: User knows this well (increases review interval)
     * - LEARNING: User is still learning (moderate interval)
     * - HARD: User finds this difficult (short interval for more practice)
     */
    @PostMapping("/{id}/progress")
    public ResponseEntity<ProgressResponse> updateProgress(
            @CurrentUser UserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody UpdateProgressRequest request
    ) {
        String userId = getUserId(userDetails);
        ProgressResponse response = vocabularyService.updateProgress(userId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get available categories.
     *
     * GET /api/vocabulary/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Category[]> getCategories() {
        return ResponseEntity.ok(Category.values());
    }

    /**
     * Get available difficulty levels.
     *
     * GET /api/vocabulary/difficulties
     */
    @GetMapping("/difficulties")
    public ResponseEntity<Difficulty[]> getDifficulties() {
        return ResponseEntity.ok(Difficulty.values());
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
