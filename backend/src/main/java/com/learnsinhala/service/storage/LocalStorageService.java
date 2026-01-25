package com.learnsinhala.service.storage;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.learnsinhala.exception.ApiException;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Local filesystem storage implementation.
 *
 * File structure:
 * storage/
 * ├── audio/
 * │   ├── greetings/
 * │   │   ├── ayubowan.mp3
 * │   │   └── kohomada.mp3
 * │   ├── numbers/
 * │   └── food/
 * └── images/
 *
 * Can be swapped for cloud storage by implementing StorageService.
 */
@Service
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local.base-path:./storage}")
    private String basePath;

    @Value("${app.storage.local.base-url:/api/files}")
    private String baseUrl;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(basePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("Storage initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(MultipartFile file, String category, String filename) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("Cannot store empty file");
        }

        try {
            return storeInternal(file.getBytes(), category, filename);
        } catch (IOException e) {
            log.error("Failed to store file: {}", filename, e);
            throw ApiException.badRequest("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public String store(byte[] bytes, String category, String filename) {
        return storeInternal(bytes, category, filename);
    }

    private String storeInternal(byte[] bytes, String category, String filename) {
        // Sanitize inputs
        String safeCategory = sanitizePath(category);
        String safeFilename = sanitizeFilename(filename);

        // Build path: category/filename
        String relativePath = safeCategory + "/" + safeFilename;
        Path targetPath = rootLocation.resolve(relativePath).normalize();

        // Security check: ensure path is within root
        if (!targetPath.startsWith(rootLocation)) {
            throw ApiException.badRequest("Invalid file path");
        }

        try {
            // Create category directory if needed
            Files.createDirectories(targetPath.getParent());

            // Write file
            Files.write(targetPath, bytes);

            log.debug("Stored file: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Failed to store file: {}", relativePath, e);
            throw ApiException.badRequest("Failed to store file");
        }
    }

    @Override
    public Resource load(String path) {
        try {
            Path filePath = rootLocation.resolve(path).normalize();

            // Security check
            if (!filePath.startsWith(rootLocation)) {
                throw ApiException.badRequest("Invalid file path");
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw ApiException.notFound("File not found: " + path);
            }
        } catch (MalformedURLException e) {
            throw ApiException.notFound("File not found: " + path);
        }
    }

    @Override
    public boolean delete(String path) {
        try {
            Path filePath = rootLocation.resolve(path).normalize();

            // Security check
            if (!filePath.startsWith(rootLocation)) {
                return false;
            }

            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", path, e);
            return false;
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = rootLocation.resolve(path).normalize();

        // Security check
        if (!filePath.startsWith(rootLocation)) {
            return false;
        }

        return Files.exists(filePath);
    }

    @Override
    public String getPublicUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        // Return URL path that will be handled by FileController
        return baseUrl + "/" + path;
    }

    /**
     * Sanitize path component (category name).
     */
    private String sanitizePath(String path) {
        if (path == null || path.isBlank()) {
            return "default";
        }
        // Allow only alphanumeric, dash, underscore
        return path.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "")
                .substring(0, Math.min(path.length(), 50));
    }

    /**
     * Sanitize filename, preserving extension.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw ApiException.badRequest("Filename required");
        }

        // Extract extension
        String ext = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = filename.substring(dotIndex).toLowerCase();
            filename = filename.substring(0, dotIndex);
        }

        // Sanitize name part
        String safeName = filename.toLowerCase()
                .replaceAll("[^a-z0-9\\-_]", "_")
                .replaceAll("_+", "_")
                .substring(0, Math.min(filename.length(), 100));

        return safeName + ext;
    }
}
