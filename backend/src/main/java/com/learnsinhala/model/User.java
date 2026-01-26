package com.learnsinhala.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User document - stores authentication and preferences.
 *
 * Indexes:
 * - email (unique) - for login lookups
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String firstName;
    
    private String lastName;

    @Indexed(unique = true)
    private String email;

    @org.springframework.data.mongodb.core.mapping.Field("passwordHash")
    private String password;

    private String displayName;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private UserPreferences preferences = new UserPreferences();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferences {
        @Builder.Default
        private int dailyGoal = 10;  // words per day

        @Builder.Default
        private Difficulty preferredDifficulty = Difficulty.BEGINNER;

        @Builder.Default
        private boolean audioEnabled = true;
    }
}
