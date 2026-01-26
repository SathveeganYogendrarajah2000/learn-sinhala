import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { AudioPlayerComponent } from '@shared/components/audio-player/audio-player.component';
import { ApiService } from '@core/services/api.service';
import { NotificationService } from '@core/services/notification.service';
import { PracticeWord, PracticeSession, AnswerResult } from '@core/models/practice.model';
import { environment } from '@env/environment';

type PracticeState = 'loading' | 'practicing' | 'revealed' | 'complete' | 'empty';

@Component({
  selector: 'app-practice',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, AudioPlayerComponent],
  template: `
    <app-layout>
      <div class="practice-container">
        @switch (state()) {
          @case ('loading') {
            <app-loading message="Loading practice session..." />
          }

          @case ('empty') {
            <div class="empty-state card">
              <h2>All caught up!</h2>
              <p>No words to practice right now. Check back later or add more vocabulary.</p>
              <button class="btn btn-primary" (click)="goToDashboard()">
                Back to Dashboard
              </button>
            </div>
          }

          @case ('complete') {
            <div class="complete-state card">
              <h2>Session Complete!</h2>
              <div class="session-summary">
                <div class="summary-stat">
                  <span class="value">{{ sessionStats().correct }}</span>
                  <span class="label">Correct</span>
                </div>
                <div class="summary-stat">
                  <span class="value">{{ sessionStats().wrong }}</span>
                  <span class="label">Needs Review</span>
                </div>
                <div class="summary-stat">
                  <span class="value">{{ sessionStats().skipped }}</span>
                  <span class="label">Skipped</span>
                </div>
              </div>
              <div class="complete-actions">
                <button class="btn btn-primary" (click)="startNewSession()">
                  Practice More
                </button>
                <button class="btn btn-secondary" (click)="goToDashboard()">
                  Back to Dashboard
                </button>
              </div>
            </div>
          }

          @default {
            <!-- Progress bar -->
            <div class="progress-bar">
              <div
                class="progress-fill"
                [style.width.%]="progressPercent()"
              ></div>
            </div>
            <div class="progress-text">
              {{ currentIndex() + 1 }} / {{ totalWords() }}
            </div>

            <!-- Flashcard -->
            <div class="flashcard card" [class.revealed]="state() === 'revealed'">
              <!-- Question side (Tamil/English) -->
              <div class="card-question">
                <span class="badge" [class.new]="currentWord().isNew">
                  {{ currentWord().isNew ? 'New Word' : 'Review' }}
                </span>

                <div class="prompt">
                  <p class="prompt-label">What is this in Sinhala?</p>
                  <p class="prompt-tamil">{{ currentWord().tamil }}</p>
                  <p class="prompt-english">({{ currentWord().english }})</p>
                </div>
              </div>

              <!-- Answer side (Sinhala) -->
              @if (state() === 'revealed') {
                <div class="card-answer">
                  <div class="answer-main">
                    <p class="sinhala">{{ currentWord().sinhala }}</p>
                    <p class="pronunciation">/{{ currentWord().pronunciation }}/</p>
                  </div>

                  @if (currentWord().audioUrl) {
                    <app-audio-player
                      [src]="getAudioUrl(currentWord().audioUrl)"
                      class="audio-player"
                    />
                  }

                  @if (currentWord().exampleSinhala) {
                    <div class="example">
                      <p class="example-sinhala">{{ currentWord().exampleSinhala }}</p>
                      <p class="example-english">{{ currentWord().exampleEnglish }}</p>
                    </div>
                  }
                </div>
              }
            </div>

            <!-- Actions -->
            <div class="actions">
              @if (state() === 'practicing') {
                <button
                  class="btn btn-primary btn-lg reveal-btn"
                  (click)="reveal()"
                >
                  Show Answer
                </button>
                <button
                  class="btn btn-outline skip-btn"
                  (click)="skip()"
                >
                  Skip
                </button>
              } @else {
                <p class="rate-prompt">How well did you know this?</p>
                <div class="rate-buttons">
                  <button
                    class="btn rate-btn rate-hard"
                    [disabled]="submitting()"
                    (click)="submitAnswer('WRONG')"
                  >
                    <span class="rate-icon">&#10060;</span>
                    <span class="rate-label">Didn't Know</span>
                    <span class="rate-hint">Review soon</span>
                  </button>
                  <button
                    class="btn rate-btn rate-ok"
                    [disabled]="submitting()"
                    (click)="submitAnswer('CORRECT')"
                  >
                    <span class="rate-icon">&#10004;</span>
                    <span class="rate-label">Got It</span>
                    <span class="rate-hint">Review later</span>
                  </button>
                </div>
              }
            </div>

            <!-- Keyboard hints -->
            <div class="keyboard-hints">
              @if (state() === 'practicing') {
                <span>Press <kbd>Space</kbd> to reveal</span>
              } @else {
                <span><kbd>1</kbd> Didn't Know</span>
                <span><kbd>2</kbd> Got It</span>
              }
            </div>
          }
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .practice-container {
      max-width: 500px;
      margin: 0 auto;
      padding: 1rem;
    }

    /* Progress */
    .progress-bar {
      height: 6px;
      background: var(--bg-tertiary);
      border-radius: 3px;
      overflow: hidden;
      margin-bottom: 0.5rem;
    }

    .progress-fill {
      height: 100%;
      background: var(--primary);
      transition: width 0.3s ease;
    }

    .progress-text {
      text-align: center;
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 1.5rem;
    }

    /* Flashcard */
    .flashcard {
      min-height: 300px;
      display: flex;
      flex-direction: column;
      margin-bottom: 1.5rem;
      transition: all 0.3s ease;
    }

    .card-question {
      text-align: center;
      padding: 1rem 0;
    }

    .badge {
      display: inline-block;
      font-size: 0.75rem;
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      background: var(--bg-tertiary);
      color: var(--text-secondary);
      margin-bottom: 1.5rem;
    }

    .badge.new {
      background: #dbeafe;
      color: #1e40af;
    }

    .prompt {
      margin-bottom: 1rem;
    }

    .prompt-label {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 1rem;
    }

    .prompt-tamil {
      font-size: 1.75rem;
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: 0.5rem;
    }

    .prompt-english {
      font-size: 1.125rem;
      color: var(--text-secondary);
    }

    /* Answer */
    .card-answer {
      border-top: 1px solid var(--border);
      padding-top: 1.5rem;
      margin-top: 1rem;
      text-align: center;
      animation: fadeIn 0.3s ease;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .answer-main {
      margin-bottom: 1rem;
    }

    .sinhala {
      font-size: 2rem;
      font-weight: 700;
      color: var(--primary);
      margin-bottom: 0.25rem;
    }

    .pronunciation {
      font-size: 1rem;
      color: var(--text-secondary);
      font-style: italic;
    }

    .audio-player {
      margin: 1rem 0;
    }

    .example {
      background: var(--bg-secondary);
      padding: 0.75rem;
      border-radius: var(--radius);
      margin-top: 1rem;
    }

    .example-sinhala {
      font-weight: 500;
      margin-bottom: 0.25rem;
    }

    .example-english {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    /* Actions */
    .actions {
      text-align: center;
      margin-bottom: 1rem;
    }

    .reveal-btn {
      width: 100%;
      padding: 1rem;
      font-size: 1.125rem;
      margin-bottom: 0.75rem;
    }

    .skip-btn {
      font-size: 0.875rem;
    }

    .rate-prompt {
      font-size: 0.875rem;
      color: var(--text-secondary);
      margin-bottom: 1rem;
    }

    .rate-buttons {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }

    .rate-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 1rem;
      border: 2px solid var(--border);
      background: var(--bg-primary);
      transition: all 0.2s ease;
    }

    .rate-btn:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }

    .rate-hard:hover:not(:disabled) {
      border-color: var(--danger);
      background: #fef2f2;
    }

    .rate-ok:hover:not(:disabled) {
      border-color: var(--secondary);
      background: #ecfdf5;
    }

    .rate-icon {
      font-size: 1.5rem;
      margin-bottom: 0.5rem;
    }

    .rate-label {
      font-weight: 600;
      margin-bottom: 0.25rem;
    }

    .rate-hint {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    /* Keyboard hints */
    .keyboard-hints {
      display: flex;
      justify-content: center;
      gap: 1.5rem;
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    kbd {
      display: inline-block;
      padding: 0.125rem 0.375rem;
      background: var(--bg-tertiary);
      border: 1px solid var(--border);
      border-radius: 4px;
      font-family: inherit;
      font-size: 0.7rem;
    }

    /* Empty & Complete states */
    .empty-state, .complete-state {
      text-align: center;
      padding: 3rem 2rem;
    }

    .empty-state h2, .complete-state h2 {
      margin-bottom: 1rem;
    }

    .empty-state p {
      color: var(--text-secondary);
      margin-bottom: 1.5rem;
    }

    .session-summary {
      display: flex;
      justify-content: center;
      gap: 2rem;
      margin: 2rem 0;
    }

    .summary-stat {
      text-align: center;
    }

    .summary-stat .value {
      display: block;
      font-size: 2rem;
      font-weight: 700;
      color: var(--primary);
    }

    .summary-stat .label {
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .complete-actions {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
  `],
  host: {
    '(document:keydown)': 'onKeydown($event)'
  }
})
export class PracticeComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);
  private notification = inject(NotificationService);

  // State
  state = signal<PracticeState>('loading');
  submitting = signal(false);

  // Session data
  words = signal<PracticeWord[]>([]);
  currentIndex = signal(0);
  sessionStats = signal({ correct: 0, wrong: 0, skipped: 0 });

  // Computed
  currentWord = computed(() => this.words()[this.currentIndex()] ?? null);
  totalWords = computed(() => this.words().length);
  progressPercent = computed(() =>
    this.totalWords() > 0 ? (this.currentIndex() / this.totalWords()) * 100 : 0
  );

  ngOnInit(): void {
    this.loadSession();
  }

  private loadSession(): void {
    this.state.set('loading');

    this.api.getTodaysPractice().subscribe({
      next: (session) => {
        if (session.words.length === 0) {
          this.state.set('empty');
        } else {
          this.words.set(session.words);
          this.currentIndex.set(0);
          this.sessionStats.set({ correct: 0, wrong: 0, skipped: 0 });
          this.state.set('practicing');
        }
      },
      error: () => {
        this.state.set('empty');
      }
    });
  }

  reveal(): void {
    this.state.set('revealed');
  }

  skip(): void {
    this.sessionStats.update(stats => ({ ...stats, skipped: stats.skipped + 1 }));
    this.nextWord();
  }

  submitAnswer(result: AnswerResult): void {
    const word = this.currentWord();
    if (!word || this.submitting()) return;

    this.submitting.set(true);

    this.api.submitAnswer({
      vocabularyId: word.id,
      result
    }).subscribe({
      next: () => {
        this.submitting.set(false);

        // Update stats
        if (result === 'CORRECT') {
          this.sessionStats.update(s => ({ ...s, correct: s.correct + 1 }));
        } else if (result === 'WRONG') {
          this.sessionStats.update(s => ({ ...s, wrong: s.wrong + 1 }));
        }

        this.nextWord();
      },
      error: () => {
        this.submitting.set(false);
        this.nextWord();
      }
    });
  }

  private nextWord(): void {
    const nextIndex = this.currentIndex() + 1;

    if (nextIndex >= this.totalWords()) {
      this.state.set('complete');
      const stats = this.sessionStats();
      this.notification.success(
        `Session complete! ${stats.correct} correct, ${stats.wrong} to review`
      );
    } else {
      this.currentIndex.set(nextIndex);
      this.state.set('practicing');
    }
  }

  startNewSession(): void {
    this.loadSession();
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  getAudioUrl(path: string | null | undefined): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return `${environment.apiUrl.replace('/api', '')}${path}`;
  }

  // Keyboard navigation
  onKeydown(event: KeyboardEvent): void {
    if (this.state() === 'practicing') {
      if (event.code === 'Space') {
        event.preventDefault();
        this.reveal();
      }
    } else if (this.state() === 'revealed') {
      if (event.key === '1') {
        this.submitAnswer('WRONG');
      } else if (event.key === '2') {
        this.submitAnswer('CORRECT');
      }
    }
  }
}
