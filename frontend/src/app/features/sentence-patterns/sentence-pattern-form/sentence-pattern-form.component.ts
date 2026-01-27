import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { ApiService } from '@core/services/api.service';
import { NotificationService } from '@core/services/notification.service';
import { SentencePattern } from '@core/models/sentence-pattern.model';
import { Category, Difficulty } from '@core/models/vocabulary.model';

@Component({
  selector: 'app-sentence-pattern-form',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, ReactiveFormsModule],
  template: `
    <app-layout>
      <div class="form-container">
        <header class="page-header">
          <h1>{{ isEditMode() ? 'Edit' : 'Add New' }} Sentence Pattern</h1>
        </header>

        @if (loading()) {
          <app-loading />
        }

        @if (!loading()) {
          <form [formGroup]="form" (ngSubmit)="onSubmit()" class="card">
            <!-- Name -->
            <div class="form-group">
              <label for="name">Pattern Name *</label>
              <input 
                id="name" 
                type="text" 
                formControlName="name"
                placeholder="e.g., Asking preferences"
              />
              @if (form.get('name')?.invalid && form.get('name')?.touched) {
                <span class="error">Pattern name is required</span>
              }
            </div>

            <!-- Sinhala Pattern -->
            <div class="form-group">
              <label for="sinhalaPattern">Sinhala Pattern (with placeholders) *</label>
              <input 
                id="sinhalaPattern" 
                type="text" 
                formControlName="sinhalaPattern"
                placeholder="e.g., (subject) (object) kanna kamatida?"
              />
              <small>Use English letters only, add placeholders using curly braces like: subject, object</small>
              @if (form.get('sinhalaPattern')?.invalid && form.get('sinhalaPattern')?.touched) {
                <span class="error">Sinhala pattern is required</span>
              }
            </div>

            <!-- English Pattern -->
            <div class="form-group">
              <label for="englishPattern">English Pattern *</label>
              <input 
                id="englishPattern" 
                type="text" 
                formControlName="englishPattern"
                placeholder="e.g., Do {subject} like to eat {object}?"
              />
              @if (form.get('englishPattern')?.invalid && form.get('englishPattern')?.touched) {
                <span class="error">English pattern is required</span>
              }
            </div>

            <!-- Tamil Pattern (Optional) -->
            <div class="form-group">
              <label for="tamilPattern">Tamil Pattern (Optional)</label>
              <input 
                id="tamilPattern" 
                type="text" 
                formControlName="tamilPattern"
                placeholder="Tamil equivalent pattern"
              />
            </div>

            <!-- Category -->
            <div class="form-group">
              <label for="category">Category *</label>
              <select id="category" formControlName="category">
                <option value="">Select category</option>
                @for (cat of categories(); track cat) {
                  <option [value]="cat">{{ formatCategory(cat) }}</option>
                }
              </select>
              @if (form.get('category')?.invalid && form.get('category')?.touched) {
                <span class="error">Category is required</span>
              }
            </div>

            <!-- Difficulty -->
            <div class="form-group">
              <label for="difficulty">Difficulty *</label>
              <select id="difficulty" formControlName="difficulty">
                <option value="">Select difficulty</option>
                @for (diff of difficulties(); track diff) {
                  <option [value]="diff">{{ formatDifficulty(diff) }}</option>
                }
              </select>
              @if (form.get('difficulty')?.invalid && form.get('difficulty')?.touched) {
                <span class="error">Difficulty is required</span>
              }
            </div>

            <!-- Usage Notes -->
            <div class="form-group">
              <label for="usageNotes">Usage Notes (Optional)</label>
              <textarea 
                id="usageNotes" 
                formControlName="usageNotes"
                rows="3"
                placeholder="Explanation of when/how to use this pattern"
              ></textarea>
            </div>

            <!-- Audio URL (Optional) -->
            <div class="form-group">
              <label for="audioUrl">Audio URL (Optional)</label>
              <input 
                id="audioUrl" 
                type="text" 
                formControlName="audioUrl"
                placeholder="https://..."
              />
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button 
                type="button" 
                class="btn btn-secondary" 
                (click)="onCancel()"
              >
                Cancel
              </button>
              <button 
                type="submit" 
                class="btn btn-primary"
                [disabled]="form.invalid || submitting()"
              >
                {{ submitting() ? 'Saving...' : (isEditMode() ? 'Update' : 'Create') }}
              </button>
            </div>
          </form>
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .form-container {
      max-width: 700px;
      margin: 0 auto;
      padding: 2rem 1rem;
    }

    .page-header {
      margin-bottom: 2rem;
    }

    .page-header h1 {
      margin: 0;
    }

    .form-group {
      margin-bottom: 1.5rem;
    }

    .form-group label {
      display: block;
      font-weight: 500;
      margin-bottom: 0.5rem;
      color: var(--text-primary);
    }

    .form-group input,
    .form-group select,
    .form-group textarea {
      width: 100%;
      padding: 0.75rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      font-size: 1rem;
      font-family: inherit;
      background: var(--bg-primary);
    }

    .form-group textarea {
      resize: vertical;
    }

    .form-group small {
      display: block;
      margin-top: 0.25rem;
      font-size: 0.875rem;
      color: var(--text-muted);
    }

    .form-group .error {
      display: block;
      margin-top: 0.25rem;
      font-size: 0.875rem;
      color: var(--error);
    }

    .form-group input:focus,
    .form-group select:focus,
    .form-group textarea:focus {
      outline: none;
      border-color: var(--primary);
    }

    .form-group input.ng-invalid.ng-touched,
    .form-group select.ng-invalid.ng-touched,
    .form-group textarea.ng-invalid.ng-touched {
      border-color: var(--error);
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      margin-top: 2rem;
    }
  `]
})
export class SentencePatternFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private notification = inject(NotificationService);

  form!: FormGroup;
  loading = signal(false);
  submitting = signal(false);
  isEditMode = signal(false);
  patternId: string | null = null;

  categories = signal<string[]>([]);
  difficulties = signal<string[]>([]);

  ngOnInit(): void {
    this.initForm();
    this.loadFilters();

    // Check if edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode.set(true);
      this.patternId = id;
      this.loadPattern(id);
    }
  }

  private initForm(): void {
    this.form = this.fb.group({
      name: ['', Validators.required],
      sinhalaPattern: ['', Validators.required],
      englishPattern: ['', Validators.required],
      tamilPattern: [''],
      category: ['', Validators.required],
      difficulty: ['', Validators.required],
      usageNotes: [''],
      audioUrl: ['']
    });
  }

  private loadFilters(): void {
    this.api.getCategories().subscribe(cats => 
      this.categories.set(cats.map(c => c.toString()))
    );
    this.api.getDifficulties().subscribe(diffs => 
      this.difficulties.set(diffs.map(d => d.toString()))
    );
  }

  private loadPattern(id: string): void {
    this.loading.set(true);

    this.api.getSentencePattern(id).subscribe({
      next: (pattern: SentencePattern) => {
        this.form.patchValue({
          name: pattern.name,
          sinhalaPattern: pattern.sinhalaPattern,
          englishPattern: pattern.englishPattern,
          tamilPattern: pattern.tamilPattern || '',
          category: pattern.category,
          difficulty: pattern.difficulty,
          usageNotes: pattern.usageNotes || '',
          audioUrl: pattern.audioUrl || ''
        });
        this.loading.set(false);
      },
      error: () => {
        this.notification.error('Failed to load sentence pattern');
        this.loading.set(false);
        this.router.navigate(['/sentence-patterns']);
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    const formValue = this.form.value;

    // Remove empty optional fields
    const data = {
      name: formValue.name,
      sinhalaPattern: formValue.sinhalaPattern,
      englishPattern: formValue.englishPattern,
      category: formValue.category,
      difficulty: formValue.difficulty,
      ...(formValue.tamilPattern && { tamilPattern: formValue.tamilPattern }),
      ...(formValue.usageNotes && { usageNotes: formValue.usageNotes }),
      ...(formValue.audioUrl && { audioUrl: formValue.audioUrl })
    };

    const request$ = this.isEditMode() && this.patternId
      ? this.api.updateSentencePattern(this.patternId, data)
      : this.api.createSentencePattern(data);

    request$.subscribe({
      next: () => {
        this.notification.success(
          `Sentence pattern ${this.isEditMode() ? 'updated' : 'created'} successfully`
        );
        this.router.navigate(['/sentence-patterns']);
      },
      error: () => {
        this.notification.error(
          `Failed to ${this.isEditMode() ? 'update' : 'create'} sentence pattern`
        );
        this.submitting.set(false);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/sentence-patterns']);
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
