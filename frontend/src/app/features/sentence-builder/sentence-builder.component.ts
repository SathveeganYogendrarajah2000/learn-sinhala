import { Component, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { LayoutComponent } from '@shared/components/layout/layout.component';
import {
  SENTENCE_PATTERNS,
  SentencePattern,
  PatternSlot,
  SlotOption,
  getCategories
} from './sentence-builder.data';

type BuilderStep = 'select-pattern' | 'fill-slots' | 'result';

interface SlotSelection {
  slotId: string;
  option: SlotOption | null;
}

@Component({
  selector: 'app-sentence-builder',
  standalone: true,
  imports: [LayoutComponent, FormsModule],
  template: `
    <app-layout>
      <div class="builder-container">
        <header class="page-header">
          <h1>Sentence Builder</h1>
          <p>Build Sinhala sentences using common patterns</p>
        </header>

        @switch (step()) {
          <!-- Step 1: Select Pattern -->
          @case ('select-pattern') {
            <div class="pattern-selection">
              <h2>Choose a sentence pattern</h2>

              <!-- Category filter -->
              <div class="category-filter">
                <button
                  class="category-btn"
                  [class.active]="selectedCategory() === ''"
                  (click)="selectCategory('')"
                >
                  All
                </button>
                @for (cat of categories; track cat) {
                  <button
                    class="category-btn"
                    [class.active]="selectedCategory() === cat"
                    (click)="selectCategory(cat)"
                  >
                    {{ cat }}
                  </button>
                }
              </div>

              <!-- Pattern list -->
              <div class="pattern-list">
                @for (pattern of filteredPatterns(); track pattern.id) {
                  <button
                    class="pattern-card"
                    (click)="selectPattern(pattern)"
                  >
                    <span class="pattern-name">{{ pattern.name }}</span>
                    <span class="pattern-category">{{ pattern.category }}</span>
                    <span class="pattern-preview">{{ pattern.sinhalaPattern }}</span>
                  </button>
                }
              </div>
            </div>
          }

          <!-- Step 2: Fill Slots -->
          @case ('fill-slots') {
            <div class="slot-filling">
              <button class="back-btn" (click)="goBack()">
                &larr; Choose different pattern
              </button>

              <div class="current-pattern card">
                <h2>{{ selectedPattern()?.name }}</h2>
                <p class="pattern-template">{{ selectedPattern()?.englishPattern }}</p>
              </div>

              <!-- Live preview -->
              <div class="live-preview card">
                <div class="preview-label">Your sentence:</div>
                <div class="preview-sinhala">{{ buildSentence() || '...' }}</div>
                <div class="preview-english">{{ buildEnglish() || '...' }}</div>
              </div>

              <!-- Slot selectors -->
              <div class="slots">
                @for (slot of selectedPattern()?.slots; track slot.id) {
                  <div class="slot-group">
                    <label class="slot-label">{{ slot.label }}</label>
                    <div class="slot-options">
                      @for (option of slot.options; track option.sinhala) {
                        <button
                          class="slot-option"
                          [class.selected]="isSelected(slot.id, option)"
                          (click)="selectSlotOption(slot.id, option)"
                        >
                          <span class="option-english">{{ option.english }}</span>
                          <span class="option-sinhala">{{ option.sinhala }}</span>
                          @if (option.tamil) {
                            <span class="option-tamil">({{ option.tamil }})</span>
                          }
                        </button>
                      }
                    </div>
                  </div>
                }
              </div>

              <!-- Continue button -->
              <button
                class="btn btn-primary btn-lg continue-btn"
                [disabled]="!allSlotsFilled()"
                (click)="showResult()"
              >
                Build Sentence
              </button>
            </div>
          }

          <!-- Step 3: Result -->
          @case ('result') {
            <div class="result-view">
              <button class="back-btn" (click)="step.set('fill-slots')">
                &larr; Edit sentence
              </button>

              <div class="result-card card">
                <div class="result-label">Your Sinhala sentence:</div>

                <div class="result-sinhala">{{ buildSentence() }}</div>

                <div class="result-pronunciation">
                  /{{ buildPronunciation() }}/
                </div>

                <div class="result-translations">
                  <div class="translation">
                    <span class="lang">English:</span>
                    <span>{{ buildEnglish() }}</span>
                  </div>
                  <div class="translation">
                    <span class="lang">Tamil:</span>
                    <span>{{ buildTamil() }}</span>
                  </div>
                </div>

                <!-- Word breakdown -->
                <div class="word-breakdown">
                  <div class="breakdown-label">Word by word:</div>
                  @for (word of getWordBreakdown(); track $index) {
                    <div class="breakdown-item">
                      <span class="word-sinhala">{{ word.sinhala }}</span>
                      <span class="word-arrow">=</span>
                      <span class="word-english">{{ word.english }}</span>
                    </div>
                  }
                </div>
              </div>

              <!-- Actions -->
              <div class="result-actions">
                <button class="btn btn-primary" (click)="copyToClipboard()">
                  {{ copied() ? 'Copied!' : 'Copy Sentence' }}
                </button>
                <button class="btn btn-secondary" (click)="startNew()">
                  Build Another
                </button>
              </div>

              <!-- Examples -->
              @if (selectedPattern()?.examples?.length) {
                <div class="examples card">
                  <h3>More examples with this pattern:</h3>
                  @for (ex of selectedPattern()?.examples; track ex.sinhala) {
                    <div class="example-item">
                      <span class="ex-sinhala">{{ ex.sinhala }}</span>
                      <span class="ex-english">{{ ex.english }}</span>
                    </div>
                  }
                </div>
              }
            </div>
          }
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .builder-container {
      max-width: 600px;
      margin: 0 auto;
    }

    .page-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .page-header p {
      color: var(--text-secondary);
    }

    /* Back button */
    .back-btn {
      background: none;
      border: none;
      color: var(--text-secondary);
      cursor: pointer;
      padding: 0.5rem 0;
      margin-bottom: 1rem;
      font-size: 0.875rem;
    }

    .back-btn:hover {
      color: var(--primary);
    }

    /* Step 1: Pattern Selection */
    .pattern-selection h2 {
      margin-bottom: 1rem;
    }

    .category-filter {
      display: flex;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-bottom: 1.5rem;
    }

    .category-btn {
      padding: 0.5rem 1rem;
      border: 1px solid var(--border);
      border-radius: 999px;
      background: var(--bg-primary);
      cursor: pointer;
      font-size: 0.875rem;
      transition: all 0.2s ease;
    }

    .category-btn:hover {
      border-color: var(--primary);
    }

    .category-btn.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
    }

    .pattern-list {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .pattern-card {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      padding: 1rem;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
      cursor: pointer;
      text-align: left;
      transition: all 0.2s ease;
    }

    .pattern-card:hover {
      border-color: var(--primary);
      box-shadow: var(--shadow);
    }

    .pattern-name {
      font-weight: 600;
      margin-bottom: 0.25rem;
    }

    .pattern-category {
      font-size: 0.75rem;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
    }

    .pattern-preview {
      font-size: 0.875rem;
      color: var(--primary);
      font-style: italic;
    }

    /* Step 2: Fill Slots */
    .current-pattern {
      margin-bottom: 1rem;
    }

    .current-pattern h2 {
      margin-bottom: 0.25rem;
    }

    .pattern-template {
      color: var(--text-secondary);
    }

    .live-preview {
      background: var(--primary);
      color: white;
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .preview-label {
      font-size: 0.75rem;
      opacity: 0.8;
      margin-bottom: 0.5rem;
    }

    .preview-sinhala {
      font-size: 1.5rem;
      font-weight: 600;
      margin-bottom: 0.25rem;
    }

    .preview-english {
      font-size: 0.875rem;
      opacity: 0.9;
    }

    .slots {
      display: flex;
      flex-direction: column;
      gap: 1.5rem;
      margin-bottom: 1.5rem;
    }

    .slot-group {
    }

    .slot-label {
      display: block;
      font-weight: 600;
      margin-bottom: 0.75rem;
    }

    .slot-options {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 0.5rem;
    }

    .slot-option {
      display: flex;
      flex-direction: column;
      padding: 0.75rem;
      border: 2px solid var(--border);
      border-radius: var(--radius);
      background: var(--bg-primary);
      cursor: pointer;
      text-align: center;
      transition: all 0.2s ease;
    }

    .slot-option:hover {
      border-color: var(--primary);
    }

    .slot-option.selected {
      border-color: var(--primary);
      background: #eef2ff;
    }

    .option-english {
      font-weight: 500;
      margin-bottom: 0.25rem;
    }

    .option-sinhala {
      color: var(--primary);
      font-size: 0.875rem;
    }

    .option-tamil {
      font-size: 0.75rem;
      color: var(--text-muted);
    }

    .continue-btn {
      width: 100%;
    }

    /* Step 3: Result */
    .result-card {
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .result-label {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 1rem;
    }

    .result-sinhala {
      font-size: 2rem;
      font-weight: 700;
      color: var(--primary);
      margin-bottom: 0.5rem;
    }

    .result-pronunciation {
      font-style: italic;
      color: var(--text-secondary);
      margin-bottom: 1.5rem;
    }

    .result-translations {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
      text-align: left;
    }

    .translation {
      display: flex;
      gap: 0.5rem;
    }

    .translation .lang {
      font-weight: 500;
      color: var(--text-muted);
      min-width: 60px;
    }

    .word-breakdown {
      border-top: 1px solid var(--border);
      padding-top: 1rem;
      text-align: left;
    }

    .breakdown-label {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 0.75rem;
    }

    .breakdown-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.25rem 0;
      font-size: 0.875rem;
    }

    .word-sinhala {
      font-weight: 500;
      color: var(--primary);
    }

    .word-arrow {
      color: var(--text-muted);
    }

    .word-english {
      color: var(--text-secondary);
    }

    .result-actions {
      display: flex;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .result-actions .btn {
      flex: 1;
    }

    .examples {
      margin-top: 1rem;
    }

    .examples h3 {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 1rem;
    }

    .example-item {
      padding: 0.5rem 0;
      border-bottom: 1px solid var(--border);
    }

    .example-item:last-child {
      border-bottom: none;
    }

    .ex-sinhala {
      display: block;
      font-weight: 500;
      margin-bottom: 0.25rem;
    }

    .ex-english {
      display: block;
      font-size: 0.875rem;
      color: var(--text-secondary);
    }
  `]
})
export class SentenceBuilderComponent {
  // State
  step = signal<BuilderStep>('select-pattern');
  selectedPattern = signal<SentencePattern | null>(null);
  selectedCategory = signal<string>('');
  slotSelections = signal<Map<string, SlotOption>>(new Map());
  copied = signal(false);

  // Data
  patterns = SENTENCE_PATTERNS;
  categories = getCategories();

  // Computed
  filteredPatterns = computed(() => {
    const cat = this.selectedCategory();
    if (!cat) return this.patterns;
    return this.patterns.filter(p => p.category === cat);
  });

  // Category selection
  selectCategory(category: string): void {
    this.selectedCategory.set(category);
  }

  // Pattern selection
  selectPattern(pattern: SentencePattern): void {
    this.selectedPattern.set(pattern);
    this.slotSelections.set(new Map());

    // If no slots, go directly to result
    if (pattern.slots.length === 0) {
      this.step.set('result');
    } else {
      this.step.set('fill-slots');
    }
  }

  // Slot selection
  selectSlotOption(slotId: string, option: SlotOption): void {
    const current = new Map(this.slotSelections());
    current.set(slotId, option);
    this.slotSelections.set(current);
  }

  isSelected(slotId: string, option: SlotOption): boolean {
    const selected = this.slotSelections().get(slotId);
    return selected?.sinhala === option.sinhala;
  }

  allSlotsFilled(): boolean {
    const pattern = this.selectedPattern();
    if (!pattern) return false;

    const selections = this.slotSelections();
    return pattern.slots.every(slot => selections.has(slot.id));
  }

  // Build sentence
  buildSentence(): string {
    const pattern = this.selectedPattern();
    if (!pattern) return '';

    let sentence = pattern.sinhalaPattern;
    const selections = this.slotSelections();

    for (const slot of pattern.slots) {
      const option = selections.get(slot.id);
      if (option) {
        sentence = sentence.replace(slot.placeholder, option.sinhala);
      }
    }

    return sentence;
  }

  buildEnglish(): string {
    const pattern = this.selectedPattern();
    if (!pattern) return '';

    let sentence = pattern.englishPattern;
    const selections = this.slotSelections();

    for (const slot of pattern.slots) {
      const option = selections.get(slot.id);
      if (option) {
        sentence = sentence.replace(slot.placeholder, option.english);
      }
    }

    return sentence;
  }

  buildTamil(): string {
    const pattern = this.selectedPattern();
    if (!pattern) return '';

    const selections = this.slotSelections();
    const parts: string[] = [];

    for (const slot of pattern.slots) {
      const option = selections.get(slot.id);
      if (option?.tamil) {
        parts.push(option.tamil);
      }
    }

    return parts.join(' ') || '...';
  }

  buildPronunciation(): string {
    // Simple pronunciation - just add hyphens between syllables
    return this.buildSentence().split(' ').join(' - ');
  }

  getWordBreakdown(): { sinhala: string; english: string }[] {
    const pattern = this.selectedPattern();
    if (!pattern) return [];

    const selections = this.slotSelections();
    const breakdown: { sinhala: string; english: string }[] = [];

    // Get selected slot values
    for (const slot of pattern.slots) {
      const option = selections.get(slot.id);
      if (option) {
        breakdown.push({
          sinhala: option.sinhala,
          english: option.english
        });
      }
    }

    // Add pattern structure words
    const structureWords = this.getStructureWords();
    breakdown.push(...structureWords);

    return breakdown;
  }

  private getStructureWords(): { sinhala: string; english: string }[] {
    const pattern = this.selectedPattern();
    if (!pattern) return [];

    // Extract non-slot words from pattern
    const id = pattern.id;

    // Common structure words based on pattern
    const structureMap: Record<string, { sinhala: string; english: string }[]> = {
      'want-to-go': [{ sinhala: 'yanna ona', english: 'want to go' }],
      'want-to-eat': [{ sinhala: 'kanna ona', english: 'want to eat' }],
      'want-to-drink': [{ sinhala: 'bonna ona', english: 'want to drink' }],
      'give-me': [
        { sinhala: 'Karunakara', english: 'please' },
        { sinhala: 'mata', english: 'to me' },
        { sinhala: 'denna', english: 'give' }
      ],
      'where-is': [{ sinhala: 'koheda', english: 'where' }],
      'how-much': [
        { sinhala: 'Meka', english: 'this' },
        { sinhala: 'kiyada', english: 'how much' }
      ],
      'i-am-going': [{ sinhala: 'yanawa', english: 'going' }],
      'i-like': [{ sinhala: 'kamathi', english: 'like' }],
    };

    return structureMap[id] || [];
  }

  // Navigation
  showResult(): void {
    this.step.set('result');
  }

  goBack(): void {
    this.step.set('select-pattern');
    this.selectedPattern.set(null);
    this.slotSelections.set(new Map());
  }

  startNew(): void {
    this.step.set('select-pattern');
    this.selectedPattern.set(null);
    this.slotSelections.set(new Map());
    this.copied.set(false);
  }

  // Copy to clipboard
  async copyToClipboard(): Promise<void> {
    const sentence = this.buildSentence();
    try {
      await navigator.clipboard.writeText(sentence);
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  }
}
