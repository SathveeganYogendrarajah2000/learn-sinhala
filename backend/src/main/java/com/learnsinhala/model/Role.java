package com.learnsinhala.model;

/**
 * User role enumeration.
 *
 * Role hierarchy and permissions:
 * - USER: Default role for new registrations. Can create, view, update, and delete own content only.
 * - ADMIN: Can view and update all content created by any user. Can manage user accounts (enable/disable).
 * - SUPERADMIN: Full system access. Can promote users to ADMIN, delete users, and manage all content.
 *
 * Note: The first SUPERADMIN must be manually set in MongoDB Atlas.
 * Subsequent ADMINs can be promoted by SUPERADMIN through the UI.
 */
public enum Role {
    /**
     * Default role for new user registrations.
     * Can only manage their own content.
     */
    USER,

    /**
     * Administrator role.
     * Can view and edit all content, manage users (enable/disable).
     */
    ADMIN,

    /**
     * Super administrator role.
     * Full access including user role promotion and deletion.
     */
    SUPERADMIN
}
