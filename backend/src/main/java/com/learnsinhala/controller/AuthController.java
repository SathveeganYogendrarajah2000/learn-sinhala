package com.learnsinhala.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnsinhala.dto.auth.AuthResponse;
import com.learnsinhala.dto.auth.LoginRequest;
import com.learnsinhala.dto.auth.RegisterRequest;
import com.learnsinhala.service.AuthService;

import lombok.RequiredArgsConstructor;

/**
 * Authentication endpoints.
 *
 * All endpoints under /api/auth/** are public (configured in SecurityConfig).
 *
 * Endpoints:
 * - POST /api/auth/register - Create new account
 * - POST /api/auth/login    - Authenticate and get JWT
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * Request body:
     * {
     *   "username": "john",
     *   "password": "secret123",
     *   "displayName": "John Doe"  // optional
     * }
     *
     * Response (201 Created):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "username": "john",
     *   "displayName": "John Doe"
     * }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with credentials.
     *
     * Request body:
     * {
     *   "username": "john",
     *   "password": "secret123"
     * }
     *
     * Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs...",
     *   "tokenType": "Bearer",
     *   "username": "john",
     *   "displayName": "John Doe"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
