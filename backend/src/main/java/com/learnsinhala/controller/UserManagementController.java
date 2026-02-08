package com.learnsinhala.controller;

import com.learnsinhala.dto.user.UpdateUserRoleRequest;
import com.learnsinhala.dto.user.UpdateUserStatusRequest;
import com.learnsinhala.dto.user.UserManagementDto;
import com.learnsinhala.exception.ApiException;
import com.learnsinhala.repository.UserRepository;
import com.learnsinhala.security.CurrentUser;
import com.learnsinhala.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin endpoints for user management.
 * All endpoints require ADMIN or SUPERADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;
    private final UserRepository userRepository;

    /**
     * List all users with pagination.
     * Requires: ADMIN or SUPERADMIN
     *
     * GET /api/admin/users?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<List<UserManagementDto>> listUsers(
            @CurrentUser UserDetails userDetails,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        com.learnsinhala.model.User user = getUser(userDetails);
        List<UserManagementDto> users = userManagementService.listUsers(user, page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Update user role.
     * Requires: SUPERADMIN only
     *
     * PUT /api/admin/users/{userId}/role
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<UserManagementDto> updateUserRole(
            @CurrentUser UserDetails userDetails,
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        com.learnsinhala.model.User user = getUser(userDetails);
        UserManagementDto updatedUser = userManagementService.updateUserRole(user, userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Enable or disable user account.
     * Requires: ADMIN or SUPERADMIN
     *
     * PUT /api/admin/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<UserManagementDto> updateUserStatus(
            @CurrentUser UserDetails userDetails,
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        com.learnsinhala.model.User user = getUser(userDetails);
        UserManagementDto updatedUser = userManagementService.updateUserStatus(user, userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user.
     * Requires: SUPERADMIN only
     *
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @CurrentUser UserDetails userDetails,
            @PathVariable String userId
    ) {
        com.learnsinhala.model.User user = getUser(userDetails);
        userManagementService.deleteUser(user, userId);
        return ResponseEntity.noContent().build();
    }

    private com.learnsinhala.model.User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("User not found"));
    }
}
