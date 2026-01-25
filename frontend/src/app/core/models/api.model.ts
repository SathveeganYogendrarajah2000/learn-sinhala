/**
 * API-related type definitions.
 */

/**
 * Standard API error response.
 */
export interface ApiError {
  status: number;
  message: string;
  error?: unknown;
  timestamp?: string;
  path?: string;
}

/**
 * Dashboard statistics response.
 */
export interface DashboardStats {
  totalVocabulary: number;
  learned: number;
  mastered: number;
  inProgress: number;
  needsReview: number;
  streak: number;
  todayProgress: TodayProgress;
  recentActivity: ActivityItem[];
}

export interface TodayProgress {
  completed: number;
  goal: number;
  newWords: number;
  reviewedWords: number;
}

export interface ActivityItem {
  id: string;
  type: 'practice' | 'learned' | 'mastered';
  vocabularyId: string;
  sinhala: string;
  english: string;
  timestamp: string;
}

/**
 * User profile response.
 */
export interface UserProfile {
  id: string;
  email: string;
  displayName: string;
  createdAt: string;
  preferences: UserPreferences;
}

export interface UserPreferences {
  dailyGoal: number;
  audioEnabled: boolean;
  showTamil: boolean;
}
