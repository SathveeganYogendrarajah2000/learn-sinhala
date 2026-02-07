package com.learnsinhala.dto;

import com.learnsinhala.model.Role;
import com.learnsinhala.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private UserPreferencesDto preferences;
    private Instant createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .preferences(UserPreferencesDto.from(user.getPreferences()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesDto {
        private int dailyGoal;
        private String preferredDifficulty;
        private boolean audioEnabled;

        public static UserPreferencesDto from(User.UserPreferences prefs) {
            if (prefs == null) {
                return UserPreferencesDto.builder()
                        .dailyGoal(10)
                        .preferredDifficulty("BEGINNER")
                        .audioEnabled(true)
                        .build();
            }
            return UserPreferencesDto.builder()
                    .dailyGoal(prefs.getDailyGoal())
                    .preferredDifficulty(prefs.getPreferredDifficulty().name())
                    .audioEnabled(prefs.isAudioEnabled())
                    .build();
        }
    }
}
