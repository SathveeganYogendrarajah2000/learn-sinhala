package com.learnsinhala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.learnsinhala.service.AudioService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Audio management endpoints (admin operations).
 *
 * For serving audio files to users, see FileController.
 */
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    /**
     * Upload audio for a vocabulary item.
     *
     * POST /api/audio/{vocabularyId}
     * Content-Type: multipart/form-data
     * file: (audio file)
     *
     * Response:
     * {
     *   "audioUrl": "/api/files/audio/greetings/65a1b2c3d4e5f6789012345.mp3"
     * }
     */
    @PostMapping("/{vocabularyId}")
    public ResponseEntity<Map<String, String>> uploadAudio(
            @PathVariable String vocabularyId,
            @RequestParam("file") MultipartFile file
    ) {
        String audioUrl = audioService.uploadAudio(vocabularyId, file);

        return ResponseEntity.ok(Map.of(
                "audioUrl", audioUrl,
                "message", "Audio uploaded successfully"
        ));
    }

    /**
     * Get audio URL for a vocabulary item.
     *
     * GET /api/audio/{vocabularyId}
     *
     * Response:
     * {
     *   "audioUrl": "/api/files/audio/greetings/65a1b2c3d4e5f6789012345.mp3"
     * }
     */
    @GetMapping("/{vocabularyId}")
    public ResponseEntity<Map<String, String>> getAudioUrl(
            @PathVariable String vocabularyId
    ) {
        String audioUrl = audioService.getAudioUrl(vocabularyId);

        if (audioUrl == null) {
            return ResponseEntity.ok(Map.of(
                    "audioUrl", "",
                    "message", "No audio available"
            ));
        }

        return ResponseEntity.ok(Map.of("audioUrl", audioUrl));
    }

    /**
     * Delete audio for a vocabulary item.
     *
     * DELETE /api/audio/{vocabularyId}
     */
    @DeleteMapping("/{vocabularyId}")
    public ResponseEntity<Map<String, String>> deleteAudio(
            @PathVariable String vocabularyId
    ) {
        audioService.deleteAudio(vocabularyId);

        return ResponseEntity.ok(Map.of("message", "Audio deleted successfully"));
    }
}
