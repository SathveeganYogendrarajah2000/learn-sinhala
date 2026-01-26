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
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  preferences: UserPreferences;
  createdAt: string;
}

export interface UserPreferences {
  dailyGoal: number;
  preferredDifficulty: string;
  audioEnabled: boolean;
}
