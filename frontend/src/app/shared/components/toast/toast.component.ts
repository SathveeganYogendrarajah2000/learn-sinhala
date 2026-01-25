import { Component, inject } from '@angular/core';
import { NotificationService, Notification } from '@core/services/notification.service';

/**
 * Toast notification container component.
 * Place once in app.component.ts.
 */
@Component({
  selector: 'app-toast',
  standalone: true,
  template: `
    <div class="toast-container">
      @for (notification of notificationService.notifications(); track notification.id) {
        <div
          class="toast"
          [class]="'toast-' + notification.type"
          (click)="notificationService.dismiss(notification.id)"
        >
          <span class="toast-icon">
            @switch (notification.type) {
              @case ('success') { &#10004; }
              @case ('error') { &#10006; }
              @case ('warning') { &#9888; }
              @case ('info') { &#8505; }
            }
          </span>
          <span class="toast-message">{{ notification.message }}</span>
          <button class="toast-close" aria-label="Close">&#10005;</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1rem;
      right: 1rem;
      z-index: 1000;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      max-width: 400px;
    }

    .toast {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.875rem 1rem;
      border-radius: var(--radius);
      box-shadow: var(--shadow-lg);
      cursor: pointer;
      animation: slideIn 0.3s ease;
    }

    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .toast-success {
      background: #ecfdf5;
      border: 1px solid #10b981;
      color: #065f46;
    }

    .toast-error {
      background: #fef2f2;
      border: 1px solid #ef4444;
      color: #991b1b;
    }

    .toast-warning {
      background: #fffbeb;
      border: 1px solid #f59e0b;
      color: #92400e;
    }

    .toast-info {
      background: #eff6ff;
      border: 1px solid #3b82f6;
      color: #1e40af;
    }

    .toast-icon {
      font-size: 1rem;
      flex-shrink: 0;
    }

    .toast-message {
      flex: 1;
      font-size: 0.875rem;
      font-weight: 500;
    }

    .toast-close {
      background: none;
      border: none;
      cursor: pointer;
      opacity: 0.5;
      font-size: 0.875rem;
      padding: 0.25rem;
      transition: opacity 0.2s;

      &:hover {
        opacity: 1;
      }
    }
  `]
})
export class ToastComponent {
  notificationService = inject(NotificationService);
}
