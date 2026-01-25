import { Component, Input, Output, EventEmitter } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Vocabulary } from '@core/models/vocabulary.model';

/**
 * Reusable vocabulary card component.
 */
@Component({
  selector: 'app-vocab-card',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="vocab-card" [class.mastered]="vocab.progress?.status === 'MASTERED'">
      <a [routerLink]="['/vocabulary', vocab.id]" class="vocab-link">
        <div class="vocab-main">
          <span class="sinhala">{{ vocab.sinhala }}</span>
          <span class="english">{{ vocab.english }}</span>
        </div>
        @if (showMeta) {
          <div class="vocab-meta">
            <span class="category">{{ formatCategory(vocab.category) }}</span>
            <span class="difficulty" [attr.data-level]="vocab.difficulty">
              {{ vocab.difficulty }}
            </span>
          </div>
        }
        @if (vocab.progress && showProgress) {
          <div class="vocab-progress">
            <span class="status" [attr.data-status]="vocab.progress.status">
              {{ vocab.progress.status }}
            </span>
          </div>
        }
      </a>
    </div>
  `,
  styles: [`
    .vocab-card {
      background: var(--bg-primary);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      transition: all 0.2s ease;
    }

    .vocab-card:hover {
      border-color: var(--primary);
      box-shadow: var(--shadow);
    }

    .vocab-card.mastered {
      border-color: var(--secondary);
    }

    .vocab-link {
      display: block;
      padding: 1rem;
      text-decoration: none;
      color: inherit;
    }

    .sinhala {
      display: block;
      font-size: 1.125rem;
      font-weight: 600;
    }

    .english {
      display: block;
      color: var(--text-secondary);
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }

    .vocab-meta {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.5rem;
    }

    .category, .difficulty {
      font-size: 0.7rem;
      padding: 0.125rem 0.375rem;
      border-radius: 4px;
      background: var(--bg-tertiary);
    }

    .difficulty[data-level="BEGINNER"] { background: #dcfce7; color: #166534; }
    .difficulty[data-level="INTERMEDIATE"] { background: #fef3c7; color: #92400e; }
    .difficulty[data-level="ADVANCED"] { background: #fee2e2; color: #991b1b; }

    .vocab-progress {
      margin-top: 0.5rem;
    }

    .status {
      font-size: 0.7rem;
      padding: 0.125rem 0.375rem;
      border-radius: 4px;
    }

    .status[data-status="NEW"] { background: #e0e7ff; color: #3730a3; }
    .status[data-status="LEARNING"] { background: #fef3c7; color: #92400e; }
    .status[data-status="REVIEWING"] { background: #dbeafe; color: #1e40af; }
    .status[data-status="MASTERED"] { background: #dcfce7; color: #166534; }
  `]
})
export class VocabCardComponent {
  @Input({ required: true }) vocab!: Vocabulary;
  @Input() showMeta = true;
  @Input() showProgress = true;

  formatCategory(category: string): string {
    if (!category) return '';
    return category.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }
}
