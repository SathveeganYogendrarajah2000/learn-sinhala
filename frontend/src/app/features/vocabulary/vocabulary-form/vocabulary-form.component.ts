import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { ApiService, CreateVocabularyRequest, UpdateVocabularyRequest } from '@core/services/api.service';
import { Category, Difficulty } from '@core/models/vocabulary.model';
import { NotificationService } from '@core/services/notification.service';

@Component({
  selector: 'app-vocabulary-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, LayoutComponent, LoadingComponent],
  template: `
    <app-layout>
      <div class="container">
        <header class="page-header">
          <h1>{{ isEditMode() ? 'Edit Vocabulary' : 'Add New Vocabulary' }}</h1>
          <p>{{ isEditMode() ? 'Update vocabulary details' : 'Create a new vocabulary entry' }}</p>
        </header>

        @if (loading()) {
          <app-loading message="Loading..." />
        } @else {
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="vocab-form">
            <div class="form-grid">
              <!-- Sinhala (romanized) -->
              <div class="form-group">
                <label for="sinhala">Sinhala (Romanized) *</label>
                <input
                  id="sinhala"
                  type="text"
                  formControlName="sinhala"
                  placeholder="e.g., ayubowan"
                  [class.error]="form.get('sinhala')?.invalid && form.get('sinhala')?.touched"
                />
                @if (form.get('sinhala')?.invalid && form.get('sinhala')?.touched) {
                  <span class="error-msg">Sinhala is required</span>
                }
              </div>

              <!-- Pronunciation -->
              <div class="form-group">
                <label for="pronunciation">Pronunciation</label>
                <input
                  id="pronunciation"
                  type="text"
                  formControlName="pronunciation"
                  placeholder="e.g., a-yu-bo-wan"
                />
              </div>

              <!-- Tamil -->
              <div class="form-group">
                <label for="tamil">Tamil *</label>
                <input
                  id="tamil"
                  type="text"
                  formControlName="tamil"
                  placeholder="e.g., வணக்கம்"
                  [class.error]="form.get('tamil')?.invalid && form.get('tamil')?.touched"
                />
                @if (form.get('tamil')?.invalid && form.get('tamil')?.touched) {
                  <span class="error-msg">Tamil is required</span>
                }
              </div>

              <!-- English -->
              <div class="form-group">
                <label for="english">English *</label>
                <input
                  id="english"
                  type="text"
                  formControlName="english"
                  placeholder="e.g., hello"
                  [class.error]="form.get('english')?.invalid && form.get('english')?.touched"
                />
                @if (form.get('english')?.invalid && form.get('english')?.touched) {
                  <span class="error-msg">English is required</span>
                }
              </div>

              <!-- Category -->
              <div class="form-group">
                <label for="category">Category *</label>
                <select
                  id="category"
                  formControlName="category"
                  [class.error]="form.get('category')?.invalid && form.get('category')?.touched"
                >
                  <option value="">Select category</option>
                  @for (cat of categories(); track cat) {
                    <option [value]="cat">{{ formatCategory(cat) }}</option>
                  }
                </select>
                @if (form.get('category')?.invalid && form.get('category')?.touched) {
                  <span class="error-msg">Category is required</span>
                }
              </div>

              <!-- Difficulty -->
              <div class="form-group">
                <label for="difficulty">Difficulty *</label>
                <select
                  id="difficulty"
                  formControlName="difficulty"
                  [class.error]="form.get('difficulty')?.invalid && form.get('difficulty')?.touched"
                >
                  <option value="">Select difficulty</option>
                  @for (diff of difficulties(); track diff) {
                    <option [value]="diff">{{ formatDifficulty(diff) }}</option>
                  }
                </select>
                @if (form.get('difficulty')?.invalid && form.get('difficulty')?.touched) {
                  <span class="error-msg">Difficulty is required</span>
                }
              </div>

              <!-- Audio URL -->
              <div class="form-group full-width">
                <label for="audioUrl">Audio URL</label>
                <input
                  id="audioUrl"
                  type="text"
                  formControlName="audioUrl"
                  placeholder="e.g., /audio/greetings/ayubowan.mp3"
                />
              </div>

              <!-- Example Sinhala -->
              <div class="form-group full-width">
                <label for="exampleSinhala">Example (Sinhala)</label>
                <input
                  id="exampleSinhala"
                  type="text"
                  formControlName="exampleSinhala"
                  placeholder="Example sentence in romanized Sinhala"
                />
              </div>

              <!-- Example English -->
              <div class="form-group full-width">
                <label for="exampleEnglish">Example (English)</label>
                <input
                  id="exampleEnglish"
                  type="text"
                  formControlName="exampleEnglish"
                  placeholder="English translation of example"
                />
              </div>

              <!-- Notes -->
              <div class="form-group full-width">
                <label for="notes">Notes</label>
                <textarea
                  id="notes"
                  formControlName="notes"
                  rows="3"
                  placeholder="Usage notes or context tips"
                ></textarea>
              </div>
            </div>

            <!-- Actions -->
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" (click)="onCancel()">
                Cancel
              </button>
              <button
                type="submit"
                class="btn btn-primary"
                [disabled]="form.invalid || saving()"
              >
                {{ saving() ? 'Saving...' : (isEditMode() ? 'Update' : 'Create') }}
              </button>
            </div>
          </form>
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
    }

    .page-header p {
      color: var(--text-secondary);
    }

    .vocab-form {
      max-width: 800px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .form-group {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .form-group.full-width {
      grid-column: 1 / -1;
    }

    .form-group label {
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-primary);
    }

    .form-group input,
    .form-group select,
    .form-group textarea {
      padding: 0.5rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
      font-family: inherit;
    }

    .form-group input.error,
    .form-group select.error {
      border-color: #dc2626;
    }

    .error-msg {
      font-size: 0.75rem;
      color: #dc2626;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
    }

    @media (max-width: 600px) {
      .form-grid {
        grid-template-columns: 1fr;
      }

      .form-group.full-width {
        grid-column: 1;
      }
    }
  `]
})
export class VocabularyFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private notification = inject(NotificationService);

  loading = signal(false);
  saving = signal(false);
  categories = signal<Category[]>([]);
  difficulties = signal<Difficulty[]>([]);
  isEditMode = signal(false);
  vocabId = signal<string | null>(null);

  form: FormGroup;

  constructor() {
    this.form = this.fb.group({
      sinhala: ['', Validators.required],
      pronunciation: [''],
      tamil: ['', Validators.required],
      english: ['', Validators.required],
      category: ['', Validators.required],
      difficulty: ['', Validators.required],
      audioUrl: [''],
      exampleSinhala: [''],
      exampleEnglish: [''],
      notes: ['']
    });
  }

  ngOnInit(): void {
    this.loadFilters();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.vocabId.set(id);
      this.loadVocabulary(id);
    }
  }

  private loadFilters(): void {
    this.api.getCategories().subscribe(cats => this.categories.set(cats));
    this.api.getDifficulties().subscribe(diffs => this.difficulties.set(diffs));
  }

  private loadVocabulary(id: string): void {
    this.loading.set(true);
    this.api.getVocabulary(id).subscribe({
      next: (vocab) => {
        this.form.patchValue({
          sinhala: vocab.sinhala,
          pronunciation: vocab.pronunciation,
          tamil: vocab.tamil,
          english: vocab.english,
          category: vocab.category,
          difficulty: vocab.difficulty,
          audioUrl: vocab.audioUrl,
          exampleSinhala: vocab.exampleSinhala,
          exampleEnglish: vocab.exampleEnglish,
          notes: vocab.notes
        });
        this.loading.set(false);
      },
      error: () => {
        this.notification.error('Failed to load vocabulary');
        this.loading.set(false);
        this.router.navigate(['/vocabulary']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);

    if (this.isEditMode()) {
      this.updateVocabulary();
    } else {
      this.createVocabulary();
    }
  }

  private createVocabulary(): void {
    const request: CreateVocabularyRequest = this.form.value;

    this.api.createVocabulary(request).subscribe({
      next: () => {
        this.notification.success('Vocabulary created successfully');
        this.router.navigate(['/vocabulary']);
      },
      error: () => {
        this.notification.error('Failed to create vocabulary');
        this.saving.set(false);
      }
    });
  }

  private updateVocabulary(): void {
    const request: UpdateVocabularyRequest = this.form.value;
    const id = this.vocabId();

    if (!id) return;

    this.api.updateVocabulary(id, request).subscribe({
      next: () => {
        this.notification.success('Vocabulary updated successfully');
        this.router.navigate(['/vocabulary']);
      },
      error: () => {
        this.notification.error('Failed to update vocabulary');
        this.saving.set(false);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/vocabulary']);
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
