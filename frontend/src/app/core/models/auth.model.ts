export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  displayName?: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  username: string;
  displayName: string;
}

export interface User {
  id: string;
  username: string;
  displayName: string;
  preferences: UserPreferences;
  createdAt: string;
}

export interface UserPreferences {
  dailyGoal: number;
  preferredDifficulty: string;
  audioEnabled: boolean;
}
