import { Role } from '@core/models/role.enum';

/**
 * User management DTO matching backend UserManagementDto
 */
export interface UserManagement {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  displayName: string;
  role: Role;
  enabled: boolean;
  createdAt: string;
  vocabularyCount?: number;
  sentencePatternCount?: number;
}

export interface UpdateUserRoleRequest {
  role: Role;
}

export interface UpdateUserStatusRequest {
  enabled: boolean;
}
