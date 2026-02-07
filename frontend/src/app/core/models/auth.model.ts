import { Role } from './role.enum';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  email: string;
  displayName: string;
  role: string;  // Role as string from backend
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;  // User's role
  preferences: UserPreferences;
  createdAt: string;
}

export interface UserPreferences {
  dailyGoal: number;
  preferredDifficulty: string;
  audioEnabled: boolean;
}
