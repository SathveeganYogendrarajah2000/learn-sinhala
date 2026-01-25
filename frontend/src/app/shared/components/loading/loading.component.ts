import { Component, Input } from '@angular/core';

/**
 * Simple loading spinner component.
 */
@Component({
  selector: 'app-loading',
  standalone: true,
  template: `
    <div class="loading" [class.overlay]="overlay">
      <div class="spinner"></div>
      @if (message) {
        <p class="message">{{ message }}</p>
      }
    </div>
  `,
  styles: [`
    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      padding: 2rem;
    }

    .loading.overlay {
      position: fixed;
      inset: 0;
      background: rgba(255, 255, 255, 0.9);
      z-index: 1000;
    }

    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid var(--border);
      border-top-color: var(--primary);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }

    .message {
      color: var(--text-secondary);
      font-weight: 500;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class LoadingComponent {
  @Input() message = '';
  @Input() overlay = false;
}
