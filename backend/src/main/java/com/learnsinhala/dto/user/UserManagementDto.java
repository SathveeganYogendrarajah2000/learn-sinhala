package com.learnsinhala.dto.user;

import com.learnsinhala.model.Role;
import com.learnsinhala.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for user management - includes role and status for admin operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDto {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String displayName;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    
    // Content statistics (optional)
    private Long vocabularyCount;
    private Long sentencePatternCount;

    public static UserManagementDto from(User user) {
        return UserManagementDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserManagementDto from(User user, long vocabCount, long patternCount) {
        UserManagementDto dto = from(user);
        dto.setVocabularyCount(vocabCount);
        dto.setSentencePatternCount(patternCount);
        return dto;
    }
}
