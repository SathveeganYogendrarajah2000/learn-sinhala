import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { ApiService } from '@core/services/api.service';
import { NotificationService } from '@core/services/notification.service';
import { Vocabulary, Category, Difficulty } from '@core/models/vocabulary.model';

@Component({
  selector: 'app-vocabulary-list',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, RouterLink, FormsModule],
  template: `
    <app-layout>
      <div class="container">
        <header class="page-header">
          <div class="header-content">
            <div>
              <h1>Vocabulary</h1>
              <p>Browse and learn Sinhala words</p>
            </div>
            <div class="header-actions">
              <button class="btn btn-secondary" [routerLink]="['/vocabulary/import']">
                📥 Import CSV
              </button>
              <button class="btn btn-primary" [routerLink]="['/vocabulary/new']">
                + Add New Vocabulary
              </button>
            </div>
          </div>
        </header>

        <!-- Filters -->
        <div class="filters">
          <div class="filter-group">
            <label for="category">Category</label>
            <select
              id="category"
              [ngModel]="selectedCategory()"
              (ngModelChange)="onCategoryChange($event)"
            >
              <option value="">All Categories</option>
              @for (cat of categories(); track cat) {
                <option [value]="cat">{{ formatCategory(cat) }}</option>
              }
            </select>
          </div>

          <div class="filter-group">
            <label for="difficulty">Difficulty</label>
            <select
              id="difficulty"
              [ngModel]="selectedDifficulty()"
              (ngModelChange)="onDifficultyChange($event)"
            >
              <option value="">All Levels</option>
              @for (diff of difficulties(); track diff) {
                <option [value]="diff">{{ formatDifficulty(diff) }}</option>
              }
            </select>
          </div>

          <button class="btn btn-secondary" (click)="clearFilters()">
            Clear Filters
          </button>
        </div>

        <!-- Results -->
        @if (loading()) {
          <app-loading message="Loading vocabulary..." />
        } @else if (vocabulary().length === 0) {
          <div class="empty-state">
            <p>No vocabulary found.</p>
            @if (hasFilters()) {
              <button class="btn btn-secondary" (click)="clearFilters()">
                Clear filters
              </button>
            }
          </div>
        } @else {
          <div class="vocab-grid">
            @for (word of vocabulary(); track word.id) {
              <div class="vocab-card" [class.mastered]="word.progress?.status === 'MASTERED'">
                <a [routerLink]="['/vocabulary', word.id]" class="vocab-link">
                  <div class="vocab-main">
                    <span class="sinhala">{{ word.sinhala }}</span>
                    <span class="english">{{ word.english }}</span>
                  </div>
                  <div class="vocab-meta">
                    <span class="category">{{ formatCategory(word.category) }}</span>
                    <span class="difficulty" [attr.data-level]="word.difficulty">
                      {{ formatDifficulty(word.difficulty) }}
                    </span>
                  </div>
                  @if (word.progress) {
                    <div class="vocab-progress">
                      <span class="status" [attr.data-status]="word.progress.status">
                        {{ word.progress.status }}
                      </span>
                      <span class="streak">{{ word.progress.streak }} streak</span>
                    </div>
                  }
                </a>
                <div class="vocab-actions">
                  <button
                    class="btn-icon"
                    [routerLink]="['/vocabulary', word.id, 'edit']"
                    (click)="$event.stopPropagation()"
                    title="Edit"
                  >
                    ✏️
                  </button>
                  <button
                    class="btn-icon btn-danger"
                    (click)="onDelete(word.id, word.sinhala); $event.stopPropagation()"
                    title="Delete"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            }
          </div>

          <!-- Pagination -->
          @if (totalPages() > 1) {
            <div class="pagination">
              <button
                class="btn btn-secondary"
                [disabled]="currentPage() === 0"
                (click)="goToPage(currentPage() - 1)"
              >
                Previous
              </button>
              <span class="page-info">
                Page {{ currentPage() + 1 }} of {{ totalPages() }}
              </span>
              <button
                class="btn btn-secondary"
                [disabled]="currentPage() >= totalPages() - 1"
                (click)="goToPage(currentPage() + 1)"
              >
                Next
              </button>
            </div>
          }
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
    }

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
    }

    .header-actions {
      display: flex;
      gap: 0.75rem;
    }

    .page-header p {
      color: var(--text-secondary);
      margin: 0;
    }

    .filters {
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .filter-group label {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-secondary);
    }

    .filter-group select {
      padding: 0.5rem 1rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
      min-width: 150px;
    }

    .vocab-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1rem;
    }

    .vocab-card {
      background: var(--bg-primary);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      transition: all 0.2s ease;
      position: relative;
    }

    .vocab-card:hover {
      border-color: var(--primary);
      box-shadow: var(--shadow);
    }

    .vocab-card.mastered {
      border-color: var(--secondary);
    }

    .vocab-actions {
      position: absolute;
      top: 0.5rem;
      right: 0.5rem;
      display: flex;
      gap: 0.25rem;
      opacity: 0;
      transition: opacity 0.2s;
    }

    .vocab-card:hover .vocab-actions {
      opacity: 1;
    }

    .btn-icon {
      background: var(--bg-primary);
      border: 1px solid var(--border);
      border-radius: 4px;
      padding: 0.25rem 0.5rem;
      cursor: pointer;
      font-size: 0.875rem;
      transition: all 0.2s;
    }

    .btn-icon:hover {
      background: var(--bg-secondary);
      transform: scale(1.1);
    }

    .btn-icon.btn-danger:hover {
      background: #fee2e2;
      border-color: #dc2626;
    }

    .vocab-link {
      display: block;
      padding: 1rem;
      text-decoration: none;
      color: inherit;
    }

    .vocab-main {
      margin-bottom: 0.5rem;
    }

    .sinhala {
      display: block;
      font-size: 1.25rem;
      font-weight: 600;
      color: var(--text-primary);
    }

    .english {
      display: block;
      color: var(--text-secondary);
      margin-top: 0.25rem;
    }

    .vocab-meta {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.75rem;
    }

    .category, .difficulty {
      font-size: 0.75rem;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      background: var(--bg-tertiary);
      color: var(--text-secondary);
    }

    .difficulty[data-level="BEGINNER"] { background: #dcfce7; color: #166534; }
    .difficulty[data-level="INTERMEDIATE"] { background: #fef3c7; color: #92400e; }
    .difficulty[data-level="ADVANCED"] { background: #fee2e2; color: #991b1b; }

    .vocab-progress {
      display: flex;
      gap: 0.5rem;
      margin-top: 0.5rem;
      font-size: 0.75rem;
    }

    .status {
      padding: 0.125rem 0.375rem;
      border-radius: 4px;
    }

    .status[data-status="NEW"] { background: #e0e7ff; color: #3730a3; }
    .status[data-status="LEARNING"] { background: #fef3c7; color: #92400e; }
    .status[data-status="REVIEWING"] { background: #dbeafe; color: #1e40af; }
    .status[data-status="MASTERED"] { background: #dcfce7; color: #166534; }

    .streak {
      color: var(--text-muted);
    }

    .empty-state {
      text-align: center;
      padding: 3rem;
      color: var(--text-secondary);
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 1rem;
      margin-top: 2rem;
    }

    .page-info {
      color: var(--text-secondary);
    }
  `]
})
export class VocabularyListComponent implements OnInit {
  private api = inject(ApiService);
  private notification = inject(NotificationService);

  // State
  loading = signal(true);
  vocabulary = signal<Vocabulary[]>([]);
  categories = signal<Category[]>([]);
  difficulties = signal<Difficulty[]>([]);

  // Filters
  selectedCategory = signal<string>('');
  selectedDifficulty = signal<string>('');

  // Pagination
  currentPage = signal(0);
  pageSize = signal(12);
  totalItems = signal(0);
  totalPages = computed(() => Math.ceil(this.totalItems() / this.pageSize()));

  // Computed
  hasFilters = computed(() =>
    this.selectedCategory() !== '' || this.selectedDifficulty() !== ''
  );

  ngOnInit(): void {
    this.loadFilters();
    this.loadVocabulary();
  }

  private loadFilters(): void {
    this.api.getCategories().subscribe(cats => this.categories.set(cats));
    this.api.getDifficulties().subscribe(diffs => this.difficulties.set(diffs));
  }

  loadVocabulary(): void {
    this.loading.set(true);

    this.api.getVocabularyList({
      category: this.selectedCategory() as Category || undefined,
      difficulty: this.selectedDifficulty() as Difficulty || undefined,
      page: this.currentPage(),
      size: this.pageSize()
    }).subscribe({
      next: (response) => {
        this.vocabulary.set(response.items);
        this.totalItems.set(response.totalItems);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  onCategoryChange(category: string): void {
    this.selectedCategory.set(category);
    this.currentPage.set(0);
    this.loadVocabulary();
  }

  onDifficultyChange(difficulty: string): void {
    this.selectedDifficulty.set(difficulty);
    this.currentPage.set(0);
    this.loadVocabulary();
  }

  clearFilters(): void {
    this.selectedCategory.set('');
    this.selectedDifficulty.set('');
    this.currentPage.set(0);
    this.loadVocabulary();
  }

  goToPage(page: number): void {
    this.currentPage.set(page);
    this.loadVocabulary();
  }

  onDelete(id: string, sinhala: string): void {
    const confirmed = confirm(`Are you sure you want to delete "${sinhala}"?`);
    if (!confirmed) return;

    this.api.deleteVocabulary(id).subscribe({
      next: () => {
        this.notification.success('Vocabulary deleted successfully');
        this.loadVocabulary();
      },
      error: () => {
        this.notification.error('Failed to delete vocabulary');
      }
    });
  }

  formatCategory(category: string): string {
    if (!category) return '';
    return category.replace(/_/g, ' ').toLowerCase()
      .replace(/\b\w/g, c => c.toUpperCase());
  }

  formatDifficulty(difficulty: string): string {
    if (!difficulty) return '';
    return difficulty.charAt(0) + difficulty.slice(1).toLowerCase();
  }
}
