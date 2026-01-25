import { Component, OnInit, inject, signal, input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { AudioPlayerComponent } from '@shared/components/audio-player/audio-player.component';
import { ApiService } from '@core/services/api.service';
import { Vocabulary } from '@core/models/vocabulary.model';
import { environment } from '@env/environment';

@Component({
  selector: 'app-vocabulary-detail',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, AudioPlayerComponent, RouterLink, DecimalPipe],
  template: `
    <app-layout>
      <div class="container">
        @if (loading()) {
          <app-loading message="Loading word..." />
        } @else if (word()) {
          <div class="detail-page">
            <!-- Back link -->
            <a routerLink="/vocabulary" class="back-link">&larr; Back to list</a>

            <!-- Main card -->
            <div class="word-card card">
              <div class="word-header">
                <div class="word-main">
                  <h1 class="sinhala">{{ word()!.sinhala }}</h1>
                  <p class="pronunciation">/{{ word()!.pronunciation }}/</p>
                </div>

                @if (word()!.audioUrl) {
                  <app-audio-player [src]="getAudioUrl(word()!.audioUrl)" />
                }
              </div>

              <div class="translations">
                <div class="translation">
                  <span class="label">English</span>
                  <span class="value">{{ word()!.english }}</span>
                </div>
                @if (word()!.tamil) {
                  <div class="translation">
                    <span class="label">Tamil</span>
                    <span class="value">{{ word()!.tamil }}</span>
                  </div>
                }
              </div>

              @if (word()!.exampleSinhala) {
                <div class="example">
                  <h3>Example</h3>
                  <p class="example-sinhala">{{ word()!.exampleSinhala }}</p>
                  <p class="example-english">{{ word()!.exampleEnglish }}</p>
                </div>
              }

              @if (word()!.notes) {
                <div class="notes">
                  <h3>Notes</h3>
                  <p>{{ word()!.notes }}</p>
                </div>
              }

              <div class="meta">
                <span class="tag category">{{ formatCategory(word()!.category) }}</span>
                <span class="tag difficulty" [attr.data-level]="word()!.difficulty">
                  {{ word()!.difficulty }}
                </span>
                @for (tag of word()!.tags; track tag) {
                  <span class="tag">{{ tag }}</span>
                }
              </div>
            </div>

            <!-- Progress card -->
            <div class="progress-card card">
              <h2>Your Progress</h2>

              @if (word()!.progress) {
                <div class="progress-stats">
                  <div class="stat">
                    <span class="stat-value" [attr.data-status]="word()!.progress!.status">
                      {{ word()!.progress!.status }}
                    </span>
                    <span class="stat-label">Status</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ word()!.progress!.streak }}</span>
                    <span class="stat-label">Streak</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ word()!.progress!.accuracy | number:'1.0-0' }}%</span>
                    <span class="stat-label">Accuracy</span>
                  </div>
                  <div class="stat">
                    <span class="stat-value">{{ word()!.progress!.correctCount }}</span>
                    <span class="stat-label">Correct</span>
                  </div>
                </div>
              } @else {
                <p class="no-progress">You haven't practiced this word yet.</p>
              }

              <!-- Action buttons -->
              <div class="actions">
                <button
                  class="btn btn-success"
                  [disabled]="updating()"
                  (click)="markAs('KNOWN')"
                >
                  {{ updating() ? '...' : 'I Know This' }}
                </button>
                <button
                  class="btn btn-secondary"
                  [disabled]="updating()"
                  (click)="markAs('LEARNING')"
                >
                  {{ updating() ? '...' : 'Still Learning' }}
                </button>
                <button
                  class="btn btn-danger"
                  [disabled]="updating()"
                  (click)="markAs('HARD')"
                >
                  {{ updating() ? '...' : 'This is Hard' }}
                </button>
              </div>

              @if (feedback()) {
                <div class="feedback" [class.success]="feedbackType() === 'success'">
                  {{ feedback() }}
                </div>
              }
            </div>
          </div>
        } @else {
          <div class="not-found">
            <h2>Word not found</h2>
            <a routerLink="/vocabulary" class="btn btn-primary">Back to vocabulary</a>
          </div>
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .back-link {
      display: inline-block;
      margin-bottom: 1rem;
      color: var(--text-secondary);
      text-decoration: none;
    }

    .back-link:hover {
      color: var(--primary);
    }

    .detail-page {
      max-width: 600px;
      margin: 0 auto;
    }

    .word-card {
      margin-bottom: 1.5rem;
    }

    .word-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.5rem;
    }

    .sinhala {
      font-size: 2rem;
      margin-bottom: 0.25rem;
    }

    .pronunciation {
      color: var(--text-secondary);
      font-style: italic;
    }

    .translations {
      display: grid;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .translation {
      display: flex;
      flex-direction: column;
    }

    .translation .label {
      font-size: 0.75rem;
      text-transform: uppercase;
      color: var(--text-muted);
      margin-bottom: 0.25rem;
    }

    .translation .value {
      font-size: 1.125rem;
    }

    .example {
      background: var(--bg-secondary);
      padding: 1rem;
      border-radius: var(--radius);
      margin-bottom: 1.5rem;
    }

    .example h3 {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
    }

    .example-sinhala {
      font-size: 1.125rem;
      margin-bottom: 0.25rem;
    }

    .example-english {
      color: var(--text-secondary);
    }

    .notes {
      margin-bottom: 1.5rem;
    }

    .notes h3 {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
    }

    .meta {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .tag {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      background: var(--bg-tertiary);
      color: var(--text-secondary);
    }

    .difficulty[data-level="BEGINNER"] { background: #dcfce7; color: #166534; }
    .difficulty[data-level="INTERMEDIATE"] { background: #fef3c7; color: #92400e; }
    .difficulty[data-level="ADVANCED"] { background: #fee2e2; color: #991b1b; }

    .progress-card h2 {
      margin-bottom: 1rem;
    }

    .progress-stats {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .stat {
      text-align: center;
    }

    .stat-value {
      display: block;
      font-size: 1.5rem;
      font-weight: 600;
    }

    .stat-value[data-status="NEW"] { color: #6366f1; }
    .stat-value[data-status="LEARNING"] { color: #f59e0b; }
    .stat-value[data-status="REVIEWING"] { color: #3b82f6; }
    .stat-value[data-status="MASTERED"] { color: #10b981; }

    .stat-label {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .no-progress {
      color: var(--text-secondary);
      margin-bottom: 1.5rem;
    }

    .actions {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
    }

    .feedback {
      margin-top: 1rem;
      padding: 0.75rem;
      border-radius: var(--radius);
      background: var(--bg-secondary);
      color: var(--text-secondary);
    }

    .feedback.success {
      background: #dcfce7;
      color: #166534;
    }

    .not-found {
      text-align: center;
      padding: 3rem;
    }
  `]
})
export class VocabularyDetailComponent implements OnInit {
  private api = inject(ApiService);
  private router = inject(Router);

  // Route param (using input signal)
  id = input.required<string>();

  // State
  loading = signal(true);
  updating = signal(false);
  word = signal<Vocabulary | null>(null);
  feedback = signal('');
  feedbackType = signal<'success' | 'error'>('success');

  ngOnInit(): void {
    this.loadWord();
  }

  private loadWord(): void {
    this.loading.set(true);

    this.api.getVocabulary(this.id()).subscribe({
      next: (vocab) => {
        this.word.set(vocab);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  markAs(action: 'KNOWN' | 'LEARNING' | 'HARD'): void {
    this.updating.set(true);
    this.feedback.set('');

    this.api.updateProgress(this.id(), action).subscribe({
      next: (response) => {
        this.updating.set(false);
        this.feedbackType.set('success');
        this.feedback.set(response.message || 'Progress updated!');

        // Reload word to get updated progress
        this.loadWord();
      },
      error: (err) => {
        this.updating.set(false);
        this.feedbackType.set('error');
        this.feedback.set(err.message || 'Failed to update progress');
      }
    });
  }

  getAudioUrl(path: string | null): string {
    if (!path) return '';
    if (path.startsWith('http')) return path;
    return `${environment.apiUrl.replace('/api', '')}${path}`;
  }

  formatCategory(category: string): string {
    if (!category) return '';
    return category.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }
}
