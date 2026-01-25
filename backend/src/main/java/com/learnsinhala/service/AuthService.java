package com.learnsinhala.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.learnsinhala.dto.auth.AuthResponse;
import com.learnsinhala.dto.auth.LoginRequest;
import com.learnsinhala.dto.auth.RegisterRequest;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.User;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles user registration and authentication.
 *
 * Flow:
 * 1. Register: Validate username uniqueness -> Hash password -> Save user -> Generate token
 * 2. Login: Authenticate credentials -> Generate token
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     *
     * @param request Registration details
     * @return AuthResponse with JWT token
     * @throws ApiException if username already exists
     */
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering user: {}", request.getUsername());

        // Check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw ApiException.conflict("Username already exists");
        }

        // Create new user with hashed password
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null
                        ? request.getDisplayName()
                        : request.getUsername())
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());

        // Generate token for immediate login
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.of(token, user.getUsername(), user.getDisplayName());
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * @param request Login credentials
     * @return AuthResponse with JWT token
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for user: {}", request.getUsername());

        // Authenticate using Spring Security's AuthenticationManager
        // This delegates to UserDetailsServiceImpl and PasswordEncoder
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Fetch user for display name
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.of(token, user.getUsername(), user.getDisplayName());
    }
}
