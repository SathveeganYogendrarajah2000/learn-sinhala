import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Main layout component with navigation.
 * Used by authenticated pages.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <div class="layout">
      <header class="header">
        <div class="header-content">
          <a routerLink="/dashboard" class="logo">Learn Sinhala</a>

          <nav class="nav">
            <a routerLink="/dashboard"
               routerLinkActive="active"
               class="nav-link">
              Dashboard
            </a>
            <a routerLink="/practice"
               routerLinkActive="active"
               class="nav-link">
              Practice
            </a>
            <a routerLink="/vocabulary"
               routerLinkActive="active"
               class="nav-link">
              Vocabulary
            </a>
            <a routerLink="/sentence-builder"
               routerLinkActive="active"
               class="nav-link">
              Sentences
            </a>
          </nav>

          <div class="user-menu">
            <span class="user-name">{{ auth.currentUser()?.firstName }}</span>
            <button class="btn-logout" (click)="auth.logout()">Logout</button>
          </div>
        </div>
      </header>

      <main class="main">
        <ng-content />
      </main>
    </div>
  `,
  styles: [`
    .layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .header {
      background: var(--bg-primary);
      border-bottom: 1px solid var(--border);
      padding: 0 1rem;
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      height: 64px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .logo {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--primary);
      text-decoration: none;
    }

    .nav {
      display: flex;
      gap: 0.5rem;
    }

    .nav-link {
      padding: 0.5rem 1rem;
      border-radius: var(--radius);
      color: var(--text-secondary);
      text-decoration: none;
      font-weight: 500;
      transition: all 0.2s ease;

      &:hover {
        background: var(--bg-secondary);
        color: var(--text-primary);
      }

      &.active {
        background: var(--primary);
        color: white;
      }
    }

    .user-menu {
      display: flex;
      align-items: center;
      gap: 1rem;
    }

    .user-name {
      color: var(--text-secondary);
      font-weight: 500;
    }

    .btn-logout {
      padding: 0.5rem 1rem;
      background: transparent;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      cursor: pointer;
      font-weight: 500;
      color: var(--text-secondary);
      transition: all 0.2s ease;

      &:hover {
        border-color: var(--danger);
        color: var(--danger);
      }
    }

    .main {
      flex: 1;
      padding: 2rem 1rem;
    }
  `]
})
export class LayoutComponent {
  auth = inject(AuthService);
}
