package com.learnsinhala.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Storage service interface for file operations.
 *
 * Implementations:
 * - LocalStorageService: Local filesystem (current)
 * - S3StorageService: AWS S3 (future)
 * - GcsStorageService: Google Cloud Storage (future)
 */
public interface StorageService {

    /**
     * Store a file and return its path/key.
     *
     * @param file     The file to store
     * @param category Storage category (e.g., "audio", "images")
     * @param filename Desired filename (will be sanitized)
     * @return The storage path/key for the file
     */
    String store(MultipartFile file, String category, String filename);

    /**
     * Store raw bytes and return the path/key.
     *
     * @param bytes    File content
     * @param category Storage category
     * @param filename Desired filename
     * @return The storage path/key
     */
    String store(byte[] bytes, String category, String filename);

    /**
     * Load a file as a Resource.
     *
     * @param path The storage path/key
     * @return Resource for streaming
     */
    Resource load(String path);

    /**
     * Delete a file.
     *
     * @param path The storage path/key
     * @return true if deleted, false if not found
     */
    boolean delete(String path);

    /**
     * Check if a file exists.
     *
     * @param path The storage path/key
     * @return true if exists
     */
    boolean exists(String path);

    /**
     * Get the public URL for a file.
     * For local storage, returns relative path.
     * For cloud storage, returns signed URL or CDN URL.
     *
     * @param path The storage path/key
     * @return Public URL to access the file
     */
    String getPublicUrl(String path);
}
