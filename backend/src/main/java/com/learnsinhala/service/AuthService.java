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
        log.debug("Registering user: {}", request.getEmail());

        // Check if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already exists");
        }

        // Create new user with hashed password
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate token for immediate login
        String token = jwtTokenProvider.generateToken(user.getEmail());

        String displayName = user.getFirstName() + " " + user.getLastName();
        return AuthResponse.of(token, user.getEmail(), displayName, user.getRole().name());
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * @param request Login credentials
     * @return AuthResponse with JWT token
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for user: {}", request.getEmail());

        // Authenticate using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Fetch user for display name
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);

        log.info("User logged in successfully: {}", user.getEmail());

        String displayName = user.getFirstName() + " " + user.getLastName();
        return AuthResponse.of(token, user.getEmail(), displayName, user.getRole().name());
    }
}
