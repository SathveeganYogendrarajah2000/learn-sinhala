import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { ApiService } from '@core/services/api.service';
import { NotificationService } from '@core/services/notification.service';
import { SentencePattern } from '@core/models/sentence-pattern.model';
import { Category, Difficulty } from '@core/models/vocabulary.model';

@Component({
  selector: 'app-sentence-pattern-list',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, RouterLink, FormsModule],
  template: `
    <app-layout>
      <div class="list-container">
        <header class="page-header">
          <div>
            <h1>Sentence Patterns</h1>
            <p>Manage sentence structure templates</p>
          </div>
          <a routerLink="/sentence-patterns/new" class="btn btn-primary">
            + Add New Pattern
          </a>
        </header>

        <!-- Filters -->
        <div class="filters card">
          <div class="filter-group">
            <label>Category:</label>
            <select [(ngModel)]="selectedCategory" (ngModelChange)="onCategoryChange($event)">
              <option value="">All Categories</option>
              @for (cat of categories(); track cat) {
                <option [value]="cat">{{ formatCategory(cat) }}</option>
              }
            </select>
          </div>

          <div class="filter-group">
            <label>Difficulty:</label>
            <select [(ngModel)]="selectedDifficulty" (ngModelChange)="onDifficultyChange($event)">
              <option value="">All Levels</option>
              @for (diff of difficulties(); track diff) {
                <option [value]="diff">{{ formatDifficulty(diff) }}</option>
              }
            </select>
          </div>

          @if (hasFilters()) {
            <button class="btn btn-secondary btn-sm" (click)="clearFilters()">
              Clear Filters
            </button>
          }
        </div>

        <!-- Loading state -->
        @if (loading()) {
          <app-loading />
        }

        <!-- Pattern list -->
        @if (!loading()) {
          @if (patterns().length === 0) {
            <div class="empty-state card">
              <p>No sentence patterns found.</p>
              <a routerLink="/sentence-patterns/new" class="btn btn-primary">
                Create First Pattern
              </a>
            </div>
          } @else {
            <div class="pattern-grid">
              @for (pattern of patterns(); track pattern.id) {
                <div class="pattern-card card">
                  <div class="card-header">
                    <h3>{{ pattern.name }}</h3>
                    <div class="card-actions">
                      <a [routerLink]="['/sentence-patterns', pattern.id, 'edit']" 
                         class="btn-icon" title="Edit">
                        ✏️
                      </a>
                      <button (click)="onDelete(pattern.id, pattern.name)" 
                              class="btn-icon" title="Delete">
                        🗑️
                      </button>
                    </div>
                  </div>

                  <div class="card-body">
                    <div class="pattern-content">
                      <div class="pattern-row">
                        <span class="label">Sinhala:</span>
                        <span class="value">{{ pattern.sinhalaPattern }}</span>
                      </div>
                      <div class="pattern-row">
                        <span class="label">English:</span>
                        <span class="value">{{ pattern.englishPattern }}</span>
                      </div>
                      @if (pattern.tamilPattern) {
                        <div class="pattern-row">
                          <span class="label">Tamil:</span>
                          <span class="value">{{ pattern.tamilPattern }}</span>
                        </div>
                      }
                    </div>

                    <div class="pattern-meta">
                      <span class="badge badge-category">
                        {{ formatCategory(pattern.category) }}
                      </span>
                      <span class="badge badge-difficulty">
                        {{ formatDifficulty(pattern.difficulty) }}
                      </span>
                    </div>

                    @if (pattern.examples && pattern.examples.length > 0) {
                      <div class="examples-count">
                        {{ pattern.examples.length }} example{{ pattern.examples.length !== 1 ? 's' : '' }}
                      </div>
                    }
                  </div>
                </div>
              }
            </div>
          }
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .list-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
      gap: 1rem;
    }

    .page-header h1 {
      margin: 0 0 0.25rem 0;
    }

    .page-header p {
      margin: 0;
      color: var(--text-secondary);
    }

    .filters {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      align-items: flex-end;
      margin-bottom: 2rem;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      min-width: 160px;
    }

    .filter-group label {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-secondary);
    }

    .filter-group select {
      padding: 0.5rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
    }

    .empty-state {
      text-align: center;
      padding: 3rem 1rem;
    }

    .empty-state p {
      margin-bottom: 1rem;
      color: var(--text-secondary);
    }

    .pattern-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 1.5rem;
    }

    .pattern-card {
      display: flex;
      flex-direction: column;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
      gap: 0.75rem;
    }

    .card-header h3 {
      margin: 0;
      flex: 1;
      font-size: 1.125rem;
    }

    .card-actions {
      display: flex;
      gap: 0.5rem;
      opacity: 0;
      transition: opacity 0.2s;
    }

    .pattern-card:hover .card-actions {
      opacity: 1;
    }

    .btn-icon {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 1.125rem;
      padding: 0.25rem;
      opacity: 0.7;
      transition: opacity 0.2s;
      text-decoration: none;
    }

    .btn-icon:hover {
      opacity: 1;
    }

    .pattern-content {
      margin-bottom: 1rem;
    }

    .pattern-row {
      display: flex;
      gap: 0.75rem;
      margin-bottom: 0.5rem;
      font-size: 0.875rem;
    }

    .pattern-row .label {
      font-weight: 500;
      color: var(--text-secondary);
      min-width: 60px;
    }

    .pattern-row .value {
      flex: 1;
      font-style: italic;
      color: var(--primary);
    }

    .pattern-meta {
      display: flex;
      gap: 0.5rem;
      margin-bottom: 0.75rem;
      flex-wrap: wrap;
    }

    .badge {
      padding: 0.25rem 0.75rem;
      border-radius: 999px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .badge-category {
      background: #e0e7ff;
      color: #4338ca;
    }

    .badge-difficulty {
      background: #fef3c7;
      color: #92400e;
    }

    .examples-count {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
  `]
})
export class SentencePatternListComponent implements OnInit {
  private api = inject(ApiService);
  private notification = inject(NotificationService);

  // State
  loading = signal(true);
  patterns = signal<SentencePattern[]>([]);
  categories = signal<string[]>([]);
  difficulties = signal<string[]>([]);

  // Filters
  selectedCategory = '';
  selectedDifficulty = '';

  // Computed
  hasFilters = computed(() =>
    this.selectedCategory !== '' || this.selectedDifficulty !== ''
  );

  ngOnInit(): void {
    this.loadFilters();
    this.loadPatterns();
  }

  private loadFilters(): void {
    this.api.getCategories().subscribe(cats => 
      this.categories.set(cats.map(c => c.toString()))
    );
    this.api.getDifficulties().subscribe(diffs => 
      this.difficulties.set(diffs.map(d => d.toString()))
    );
  }

  loadPatterns(): void {
    this.loading.set(true);

    this.api.getSentencePatterns({
      category: this.selectedCategory || undefined,
      difficulty: this.selectedDifficulty || undefined
    }).subscribe({
      next: (patterns) => {
        this.patterns.set(patterns);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.notification.error('Failed to load sentence patterns');
      }
    });
  }

  onCategoryChange(category: string): void {
    this.selectedCategory = category;
    this.loadPatterns();
  }

  onDifficultyChange(difficulty: string): void {
    this.selectedDifficulty = difficulty;
    this.loadPatterns();
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.selectedDifficulty = '';
    this.loadPatterns();
  }

  onDelete(id: string, name: string): void {
    const confirmed = confirm(`Are you sure you want to delete "${name}"?`);
    if (!confirmed) return;

    this.api.deleteSentencePattern(id).subscribe({
      next: () => {
        this.notification.success('Sentence pattern deleted successfully');
        this.loadPatterns();
      },
      error: () => {
        this.notification.error('Failed to delete sentence pattern');
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
