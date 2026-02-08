package com.learnsinhala.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private String email;
    private String displayName;
    private String role;

    public static AuthResponse of(String token, String email, String displayName, String role) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(email)
                .displayName(displayName)
                .role(role)
                .build();
    }
}
