package com.learnsinhala.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learnsinhala.dto.UserDto;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.CurrentUser;

import lombok.RequiredArgsConstructor;

/**
 * User profile endpoints.
 *
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * Get current authenticated user's profile.
     *
     * Response:
     * {
     *   "id": "...",
     *   "username": "john",
     *   "displayName": "John Doe",
     *   "preferences": {
     *     "dailyGoal": 10,
     *     "preferredDifficulty": "BEGINNER",
     *     "audioEnabled": true
     *   },
     *   "createdAt": "2024-01-01T00:00:00Z"
     * }
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@CurrentUser UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> ResponseEntity.ok(UserDto.from(user)))
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
