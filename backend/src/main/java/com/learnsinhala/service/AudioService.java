package com.learnsinhala.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Vocabulary;
import com.learnsinhala.repository.VocabularyRepository;
import com.learnsinhala.service.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * Service for managing vocabulary audio files.
 *
 * Audio file organization:
 * - audio/{category}/{vocabulary-id}.mp3
 *
 * Example:
 * - audio/greetings/65a1b2c3d4e5f6789012345.mp3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AudioService {

    private final StorageService storageService;
    private final VocabularyRepository vocabularyRepository;

    private static final String AUDIO_CATEGORY = "audio";
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "audio/mpeg",       // .mp3
            "audio/mp3",
            "audio/wav",
            "audio/x-wav",
            "audio/ogg"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload audio for a vocabulary item.
     *
     * @param vocabularyId The vocabulary to attach audio to
     * @param file         Audio file (mp3, wav, ogg)
     * @return The public URL for the audio
     */
    public String uploadAudio(String vocabularyId, MultipartFile file) {
        // Validate vocabulary exists
        Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        // Validate file
        validateAudioFile(file);

        // Determine storage path: audio/{category}/{id}.{ext}
        String category = vocab.getCategory() != null
                ? vocab.getCategory().name().toLowerCase()
                : "general";

        String extension = getExtension(file.getOriginalFilename());
        String filename = vocabularyId + extension;
        String storagePath = AUDIO_CATEGORY + "/" + category;

        // Delete existing audio if any
        if (vocab.getAudioUrl() != null) {
            String existingPath = extractPathFromUrl(vocab.getAudioUrl());
            if (existingPath != null) {
                storageService.delete(existingPath);
            }
        }

        // Store new file
        String path = storageService.store(file, storagePath, filename);
        String publicUrl = storageService.getPublicUrl(path);

        // Update vocabulary with audio URL
        vocab.setAudioUrl(publicUrl);
        vocabularyRepository.save(vocab);

        log.info("Uploaded audio for vocabulary {}: {}", vocabularyId, publicUrl);

        return publicUrl;
    }

    /**
     * Get audio resource for streaming.
     *
     * @param path Storage path
     * @return Resource for streaming
     */
    public Resource getAudio(String path) {
        return storageService.load(path);
    }

    /**
     * Delete audio for a vocabulary item.
     *
     * @param vocabularyId The vocabulary to remove audio from
     */
    public void deleteAudio(String vocabularyId) {
        Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> ApiException.notFound("Vocabulary not found"));

        if (vocab.getAudioUrl() != null) {
            String path = extractPathFromUrl(vocab.getAudioUrl());
            if (path != null) {
                storageService.delete(path);
            }
            vocab.setAudioUrl(null);
            vocabularyRepository.save(vocab);

            log.info("Deleted audio for vocabulary {}", vocabularyId);
        }
    }

    /**
     * Get the public URL for a vocabulary's audio.
     *
     * @param vocabularyId Vocabulary ID
     * @return Audio URL or null if no audio
     */
    public String getAudioUrl(String vocabularyId) {
        return vocabularyRepository.findById(vocabularyId)
                .map(Vocabulary::getAudioUrl)
                .orElse(null);
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Audio file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("Audio file must be under 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw ApiException.badRequest("Invalid audio format. Allowed: MP3, WAV, OGG");
        }
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return ".mp3";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex).toLowerCase() : ".mp3";
    }

    private String extractPathFromUrl(String url) {
        // URL format: /api/files/{path}
        if (url != null && url.contains("/api/files/")) {
            return url.substring(url.indexOf("/api/files/") + 11);
        }
        return null;
    }
}
