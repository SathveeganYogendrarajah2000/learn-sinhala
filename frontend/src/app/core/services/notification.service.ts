import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface Notification {
  id: string;
  type: NotificationType;
  message: string;
  duration: number;
}

/**
 * Service for displaying toast notifications.
 * Uses signals for reactive state management.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  notifications = signal<Notification[]>([]);

  private idCounter = 0;

  /**
   * Show a success notification.
   */
  success(message: string, duration = 3000): void {
    this.show('success', message, duration);
  }

  /**
   * Show an error notification.
   */
  error(message: string, duration = 5000): void {
    this.show('error', message, duration);
  }

  /**
   * Show a warning notification.
   */
  warning(message: string, duration = 4000): void {
    this.show('warning', message, duration);
  }

  /**
   * Show an info notification.
   */
  info(message: string, duration = 3000): void {
    this.show('info', message, duration);
  }

  /**
   * Show a notification.
   */
  private show(type: NotificationType, message: string, duration: number): void {
    const id = `notification-${++this.idCounter}`;

    const notification: Notification = { id, type, message, duration };

    this.notifications.update(list => [...list, notification]);

    // Auto-dismiss after duration
    if (duration > 0) {
      setTimeout(() => this.dismiss(id), duration);
    }
  }

  /**
   * Dismiss a notification by ID.
   */
  dismiss(id: string): void {
    this.notifications.update(list => list.filter(n => n.id !== id));
  }

  /**
   * Clear all notifications.
   */
  clearAll(): void {
    this.notifications.set([]);
  }
}
