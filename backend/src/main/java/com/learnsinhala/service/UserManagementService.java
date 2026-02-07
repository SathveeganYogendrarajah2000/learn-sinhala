package com.learnsinhala.service;

import com.learnsinhala.dto.user.UpdateUserRoleRequest;
import com.learnsinhala.dto.user.UpdateUserStatusRequest;
import com.learnsinhala.dto.user.UserManagementDto;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.model.Role;
import com.learnsinhala.model.User;
import com.learnsinhala.repository.SentencePatternRepository;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.repository.VocabularyRepository;
import com.learnsinhala.security.RoleValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user management operations (ADMIN+).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SentencePatternRepository sentencePatternRepository;

    /**
     * List all users with pagination.
     * Only ADMIN and SUPERADMIN can access.
     */
    public List<UserManagementDto> listUsers(User requester, Integer page, Integer size) {
        // Check permission
        if (!RoleValidator.canManageUsers(requester)) {
            throw ApiException.forbidden("Only ADMIN and SUPERADMIN can list users");
        }

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 50,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> usersPage = userRepository.findAll(pageable);

        return usersPage.getContent().stream()
                .map(user -> {
                    // Get content counts for each user
                    long vocabCount = vocabularyRepository.countByCreatedBy(user.getId());
                    long patternCount = sentencePatternRepository.countByCreatedBy(user.getId());
                    return UserManagementDto.from(user, vocabCount, patternCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Update user role.
     * Only SUPERADMIN can perform this operation.
     */
    public UserManagementDto updateUserRole(User requester, String userId, UpdateUserRoleRequest request) {
        // Check permission - only SUPERADMIN can change roles
        if (!RoleValidator.isSuperAdmin(requester)) {
            throw ApiException.forbidden("Only SUPERADMIN can update user roles");
        }

        // Cannot modify your own role
        if (requester.getId().equals(userId)) {
            throw ApiException.badRequest("Cannot modify your own role");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        targetUser.setRole(request.getRole());
        targetUser = userRepository.save(targetUser);

        log.info("SUPERADMIN {} updated user {} role to {}", requester.getEmail(), userId, request.getRole());

        return UserManagementDto.from(targetUser);
    }

    /**
     * Enable or disable user account.
     * ADMIN and SUPERADMIN can perform this operation.
     */
    public UserManagementDto updateUserStatus(User requester, String userId, UpdateUserStatusRequest request) {
        // Check permission
        if (!RoleValidator.canManageUsers(requester)) {
            throw ApiException.forbidden("Only ADMIN and SUPERADMIN can update user status");
        }

        // Cannot modify your own status
        if (requester.getId().equals(userId)) {
            throw ApiException.badRequest("Cannot modify your own status");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        targetUser.setEnabled(request.getEnabled());
        targetUser = userRepository.save(targetUser);

        log.info("{} {} user {}", requester.getEmail(), 
                request.getEnabled() ? "enabled" : "disabled", userId);

        return UserManagementDto.from(targetUser);
    }

    /**
     * Delete user.
     * Only SUPERADMIN can perform this operation.
     */
    public void deleteUser(User requester, String userId) {
        // Check permission - only SUPERADMIN can delete users
        if (!RoleValidator.isSuperAdmin(requester)) {
            throw ApiException.forbidden("Only SUPERADMIN can delete users");
        }

        // Cannot delete yourself
        if (requester.getId().equals(userId)) {
            throw ApiException.badRequest("Cannot delete your own account");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        // Delete the user
        // Note: Content created by this user will remain (orphaned with their userId in createdBy)
        userRepository.deleteById(userId);

        log.warn("SUPERADMIN {} deleted user {}", requester.getEmail(), targetUser.getEmail());
    }
}
