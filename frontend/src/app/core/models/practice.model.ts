export interface PracticeSession {
  words: PracticeWord[];
  totalWords: number;
  reviewWords: number;
  newWords: number;
  stats: SessionStats;
}

export interface PracticeWord {
  id: string;
  sinhala: string;
  pronunciation: string;
  tamil: string;
  english: string;
  category: string;
  difficulty: string;
  audioUrl: string | null;
  exampleSinhala: string;
  exampleEnglish: string;
  tags: string[];
  isNew: boolean;
  isReview: boolean;
  currentStreak: number;
  currentStatus: string;
}

export interface SessionStats {
  totalLearned: number;
  mastered: number;
  inProgress: number;
  dailyGoal: number;
  completedToday: number;
}

export interface AnswerRequest {
  vocabularyId: string;
  result: AnswerResult;
}

export type AnswerResult = 'CORRECT' | 'WRONG' | 'SKIP';

export interface AnswerResponse {
  vocabularyId: string;
  newStatus: string;
  newStreak: number;
  correctCount: number;
  incorrectCount: number;
  accuracy: number;
  nextReviewAt: string;
  feedback: string;
}
