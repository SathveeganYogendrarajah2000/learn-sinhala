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

    public static AuthResponse of(String token, String email, String displayName) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(email)
                .displayName(displayName)
                .build();
    }
}
