package com.learnsinhala.controller;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnsinhala.service.storage.StorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Serves stored files (audio, images).
 *
 * This endpoint is public to allow audio playback without authentication.
 * Configure in SecurityConfig: .requestMatchers("/api/files/**").permitAll()
 *
 * URL pattern: /api/files/{category}/{subcategory}/{filename}
 * Example: /api/files/audio/greetings/ayubowan.mp3
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final StorageService storageService;

    // Map file extensions to MIME types
    private static final Map<String, MediaType> MEDIA_TYPES = Map.of(
            ".mp3", MediaType.parseMediaType("audio/mpeg"),
            ".wav", MediaType.parseMediaType("audio/wav"),
            ".ogg", MediaType.parseMediaType("audio/ogg"),
            ".png", MediaType.IMAGE_PNG,
            ".jpg", MediaType.IMAGE_JPEG,
            ".jpeg", MediaType.IMAGE_JPEG,
            ".gif", MediaType.IMAGE_GIF
    );

    /**
     * Serve a file from storage.
     *
     * GET /api/files/audio/greetings/kohomada.mp3
     *
     * Supports:
     * - Audio: mp3, wav, ogg
     * - Images: png, jpg, gif
     */
    @SuppressWarnings("null")
    @GetMapping("/**")
    public ResponseEntity<Resource> serveFile(HttpServletRequest request) {
        // Extract path after /api/files/
        String path = request.getRequestURI()
                .substring(request.getContextPath().length() + "/api/files/".length());

        log.debug("Serving file: {}", path);

        // Load resource
        Resource resource = storageService.load(path);

        // Determine content type
        MediaType mediaType = getMediaType(path);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400") // Cache 24h
                .body(resource);
    }

    private MediaType getMediaType(String path) {
        String extension = path.substring(path.lastIndexOf('.')).toLowerCase();
        return MEDIA_TYPES.getOrDefault(extension, MediaType.APPLICATION_OCTET_STREAM);
    }
}
