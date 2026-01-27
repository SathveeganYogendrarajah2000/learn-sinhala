export interface SentencePattern {
  id: string;
  name: string;
  sinhalaPattern: string;
  tamilPattern: string;
  englishPattern: string;
  usageNotes: string;
  category: string;
  difficulty: string;
  examples: PatternExample[];
  audioUrl: string | null;
}

export interface PatternExample {
  sinhala: string;
  english: string;
  audioUrl?: string;
}

export interface CreateSentencePatternRequest {
  name: string;
  sinhalaPattern: string;
  tamilPattern?: string;
  englishPattern: string;
  usageNotes?: string;
  category: string;
  difficulty: string;
  examples?: PatternExample[];
  audioUrl?: string;
}

export interface UpdateSentencePatternRequest {
  name?: string;
  sinhalaPattern?: string;
  tamilPattern?: string;
  englishPattern?: string;
  usageNotes?: string;
  category?: string;
  difficulty?: string;
  examples?: PatternExample[];
  audioUrl?: string;
}
