package com.learnsinhala.security;

import com.learnsinhala.model.Role;
import com.learnsinhala.model.User;

/**
 * Utility class for role-based permission validation.
 *
 * Provides methods to check if a user has permission to perform
 * various operations based on their role and content ownership.
 */
public class RoleValidator {

    /**
     * Check if user can view content.
     *
     * NEW RULE: Everyone can view all content.
     *
     * @param user User attempting to view
     * @param createdBy User ID of content creator (unused)
     * @return true always
     */
    public static boolean canViewContent(User user, String createdBy) {
        return true; // Everyone can view all content
    }

    /**
     * Check if user can edit content.
     *
     * NEW RULES:
     * - If createdBy is null (ADMIN/SUPERADMIN content), only ADMIN+ can edit
     * - If createdBy is set (USER content), only that user can edit
     *
     * @param user User attempting to edit
     * @param createdBy User ID of content creator (null for ADMIN+ content)
     * @return true if user can edit, false otherwise
     */
    public static boolean canEditContent(User user, String createdBy) {
        // Content created by ADMIN/SUPERADMIN (createdBy is null)
        if (createdBy == null) {
            return isAdminOrAbove(user); // Only ADMIN+ can edit admin content
        }
        // Content created by specific user - only that user can edit
        return user.getId().equals(createdBy);
    }

    /**
     * Check if user can delete content.
     *
     * NEW RULES:
     * - If createdBy is null (ADMIN/SUPERADMIN content), only ADMIN+ can delete
     * - If createdBy is set (USER content), only that user can delete
     *
     * @param user User attempting to delete
     * @param createdBy User ID of content creator (null for ADMIN+ content)
     * @return true if user can delete, false otherwise
     */
    public static boolean canDeleteContent(User user, String createdBy) {
        // Content created by ADMIN/SUPERADMIN (createdBy is null)
        if (createdBy == null) {
            return isAdminOrAbove(user); // Only ADMIN+ can delete admin content
        }
        // Content created by specific user - only that user can delete
        return user.getId().equals(createdBy);
    }

    /**
     * Check if user is ADMIN or SUPERADMIN.
     *
     * @param user User to check
     * @return true if user is ADMIN or SUPERADMIN
     */
    public static boolean isAdminOrAbove(User user) {
        return user.getRole() == Role.ADMIN || user.getRole() == Role.SUPERADMIN;
    }

    /**
     * Check if user is SUPERADMIN.
     *
     * @param user User to check
     * @return true if user is SUPERADMIN
     */
    public static boolean isSuperAdmin(User user) {
        return user.getRole() == Role.SUPERADMIN;
    }

    /**
     * Check if user can manage other users (view, enable/disable).
     *
     * @param user User to check
     * @return true if user can manage users
     */
    public static boolean canManageUsers(User user) {
        return isAdminOrAbove(user);
    }

    /**
     * Check if user can promote others to ADMIN role.
     *
     * @param user User to check
     * @return true if user can promote to ADMIN
     */
    public static boolean canPromoteToAdmin(User user) {
        return isSuperAdmin(user);
    }

    /**
     * Check if user can delete other users.
     *
     * @param user User to check
     * @return true if user can delete users
     */
    public static boolean canDeleteUsers(User user) {
        return isSuperAdmin(user);
    }
}
