export interface Vocabulary {
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
  notes: string;
  tags: string[];
  progress: ProgressInfo | null;
}

export interface ProgressInfo {
  status: LearningStatus;
  correctCount: number;
  incorrectCount: number;
  streak: number;
  accuracy: number;
}

export interface VocabularyListResponse {
  items: Vocabulary[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export type LearningStatus = 'NEW' | 'LEARNING' | 'REVIEWING' | 'MASTERED';
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type Category =
  | 'GREETINGS' | 'NUMBERS' | 'FOOD' | 'TRAVEL'
  | 'SHOPPING' | 'FAMILY' | 'WORK' | 'TIME'
  | 'WEATHER' | 'DIRECTIONS' | 'EMERGENCY'
  | 'DAILY_PHRASES' | 'QUESTIONS' | 'RESPONSES';
