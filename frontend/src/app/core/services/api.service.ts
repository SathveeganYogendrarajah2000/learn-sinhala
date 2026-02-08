import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, retry, timer } from 'rxjs';

import { environment } from '@env/environment';
import {
  Vocabulary,
  VocabularyListResponse,
  Category,
  Difficulty
} from '@core/models/vocabulary.model';
import {
  SentencePattern,
  CreateSentencePatternRequest,
  UpdateSentencePatternRequest
} from '@core/models/sentence-pattern.model';
import {
  PracticeSession,
  AnswerRequest,
  AnswerResponse
} from '@core/models/practice.model';
import {
  DashboardStats,
  UserProfile,
  UserPreferences
} from '@core/models/api.model';

/**
 * API service for all backend endpoints.
 * Includes retry logic for transient failures.
 */
@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly apiUrl = environment.apiUrl;
  private readonly retryConfig = {
    count: environment.api.retryAttempts,
    delay: (retryCount: number) =>
      timer(environment.api.retryDelay * retryCount)
  };

  constructor(private http: HttpClient) {}

  // ========================
  // Dashboard
  // ========================

  /**
   * Get dashboard statistics.
   */
  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/dashboard/stats`)
      .pipe(retry(this.retryConfig));
  }

  // ========================
  // User
  // ========================

  /**
   * Get current user profile.
   */
  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/users/me`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Update user preferences.
   */
  updatePreferences(preferences: Partial<UserPreferences>): Observable<UserProfile> {
    return this.http.patch<UserProfile>(
      `${this.apiUrl}/users/me/preferences`,
      preferences
    );
  }

  // ========================
  // Vocabulary
  // ========================

  /**
   * List vocabulary with optional filters.
   */
  getVocabularyList(params?: {
    category?: Category;
    difficulty?: Difficulty;
    search?: string;
    page?: number;
    size?: number;
  }): Observable<VocabularyListResponse> {
    let httpParams = new HttpParams();

    if (params?.category) {
      httpParams = httpParams.set('category', params.category);
    }
    if (params?.difficulty) {
      httpParams = httpParams.set('difficulty', params.difficulty);
    }
    if (params?.search) {
      httpParams = httpParams.set('search', params.search);
    }
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size) {
      httpParams = httpParams.set('size', params.size.toString());
    }

    return this.http.get<VocabularyListResponse>(
      `${this.apiUrl}/vocabulary`,
      { params: httpParams }
    ).pipe(retry(this.retryConfig));
  }

  /**
   * Get a single vocabulary item.
   */
  getVocabulary(id: string): Observable<Vocabulary> {
    return this.http.get<Vocabulary>(`${this.apiUrl}/vocabulary/${id}`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Get available categories.
   */
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/vocabulary/categories`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Get available difficulty levels.
   */
  getDifficulties(): Observable<Difficulty[]> {
    return this.http.get<Difficulty[]>(`${this.apiUrl}/vocabulary/difficulties`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Create new vocabulary item.
   */
  createVocabulary(data: CreateVocabularyRequest): Observable<Vocabulary> {
    return this.http.post<Vocabulary>(`${this.apiUrl}/vocabulary`, data);
  }

  /**
   * Update existing vocabulary item.
   */
  updateVocabulary(id: string, data: UpdateVocabularyRequest): Observable<Vocabulary> {
    return this.http.put<Vocabulary>(`${this.apiUrl}/vocabulary/${id}`, data);
  }

  /**
   * Delete vocabulary item.
   */
  deleteVocabulary(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/vocabulary/${id}`);
  }

  /**
   * Import vocabulary items from CSV file.
   */
  importCsvVocabulary(file: File): Observable<CsvUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<CsvUploadResponse>(
      `${this.apiUrl}/vocabulary/import`,
      formData
    );
  }

  // ========================
  // Sentence Patterns
  // ========================

  /**
   * List sentence patterns with optional filters.
   */
  getSentencePatterns(params?: {
    category?: string;
    difficulty?: string;
    page?: number;
    size?: number;
  }): Observable<SentencePattern[]> {
    let httpParams = new HttpParams();

    if (params?.category) {
      httpParams = httpParams.set('category', params.category);
    }
    if (params?.difficulty) {
      httpParams = httpParams.set('difficulty', params.difficulty);
    }
    if (params?.page !== undefined) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params?.size) {
      httpParams = httpParams.set('size', params.size.toString());
    }

    return this.http.get<SentencePattern[]>(
      `${this.apiUrl}/sentence-patterns`,
      { params: httpParams }
    ).pipe(retry(this.retryConfig));
  }

  /**
   * Get a single sentence pattern.
   */
  getSentencePattern(id: string): Observable<SentencePattern> {
    return this.http.get<SentencePattern>(`${this.apiUrl}/sentence-patterns/${id}`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Create new sentence pattern.
   */
  createSentencePattern(data: CreateSentencePatternRequest): Observable<SentencePattern> {
    return this.http.post<SentencePattern>(`${this.apiUrl}/sentence-patterns`, data);
  }

  /**
   * Update existing sentence pattern.
   */
  updateSentencePattern(id: string, data: UpdateSentencePatternRequest): Observable<SentencePattern> {
    return this.http.put<SentencePattern>(`${this.apiUrl}/sentence-patterns/${id}`, data);
  }

  /**
   * Delete sentence pattern.
   */
  deleteSentencePattern(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/sentence-patterns/${id}`);
  }

  // ========================
  // Practice
  // ========================

  /**
   * Get today's practice session.
   */
  getTodaysPractice(): Observable<PracticeSession> {
    return this.http.get<PracticeSession>(`${this.apiUrl}/practice/today`)
      .pipe(retry(this.retryConfig));
  }

  /**
   * Submit answer for a practice word.
   * No retry for mutations to avoid duplicate submissions.
   */
  submitAnswer(request: AnswerRequest): Observable<AnswerResponse> {
    return this.http.post<AnswerResponse>(
      `${this.apiUrl}/practice/answer`,
      request
    );
  }

  // ========================
  // Progress
  // ========================

  /**
   * Update vocabulary progress.
   */
  updateProgress(vocabularyId: string, action: 'KNOWN' | 'LEARNING' | 'HARD'): Observable<ProgressUpdateResponse> {
    return this.http.post<ProgressUpdateResponse>(
      `${this.apiUrl}/vocabulary/${vocabularyId}/progress`,
      { action }
    );
  }
}

export interface CsvUploadResponse {
  totalRows: number;
  successCount: number;
  errorCount: number;
  createdIds: string[];
  errors: CsvRowError[];
}

export interface CsvRowError {
  rowNumber: number;
  field?: string;
  message: string;
  rawData?: string;
}

export interface ProgressUpdateResponse {
  vocabularyId: string;
  status: string;
  correctCount: number;
  incorrectCount: number;
  streak: number;
  accuracy: number;
  nextReviewAt: string;
  message: string;
}

export interface CreateVocabularyRequest {
  sinhala: string;
  pronunciation?: string;
  tamil: string;
  english: string;
  category: Category;
  difficulty: Difficulty;
  audioUrl?: string;
  exampleSinhala?: string;
  exampleEnglish?: string;
  notes?: string;
  tags?: string[];
}

export interface UpdateVocabularyRequest {
  sinhala?: string;
  pronunciation?: string;
  tamil?: string;
  english?: string;
  category?: Category;
  difficulty?: Difficulty;
  audioUrl?: string;
  exampleSinhala?: string;
  exampleEnglish?: string;
  notes?: string;
  tags?: string[];
}
