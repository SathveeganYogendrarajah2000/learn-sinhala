import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { Role } from '@core/models/role.enum';
import { RoleAccessDirective } from '@app/shared/directives/role-access.directive';

/**
 * Main layout component with navigation.
 * Used by authenticated pages.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RoleAccessDirective],
  template: `
    <div class="layout">
      <header class="header">
        <div class="header-content">
          <a routerLink="/dashboard" class="logo">Learn Sinhala</a>

          <nav class="nav">
            <div class="nav-group">
              <a routerLink="/dashboard"
                 routerLinkActive="active"
                 [routerLinkActiveOptions]="{exact: true}"
                 class="nav-link">
                Dashboard
              </a>
              <a routerLink="/practice"
                 routerLinkActive="active"
                 class="nav-link">
                Practice
              </a>
            </div>
            
            <div class="nav-group">
              <a routerLink="/vocabulary"
                 routerLinkActive="active"
                 class="nav-link">
                Vocabulary
              </a>
              <a routerLink="/sentence-patterns"
                 routerLinkActive="active"
                 class="nav-link">
                Patterns
              </a>
              <a routerLink="/sentence-builder"
                 routerLinkActive="active"
                 class="nav-link">
                Builder
              </a>
            </div>
            
            <!-- Admin Panel Link (ADMIN+ only) -->
            <a *appRoleAccess="[Role.ADMIN, Role.SUPERADMIN]"
               routerLink="/admin/users"
               routerLinkActive="active"
               class="nav-link admin-link">
              👑 Admin
            </a>
          </nav>

          <div class="user-menu">
            <div class="user-info">
              <span class="user-name">{{ auth.currentUser()?.firstName }}</span>
              <span [class]="'role-badge role-' + auth.getUserRole().toLowerCase()">
                {{ auth.getUserRole() }}
              </span>
            </div>
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
      gap: 1rem;
      align-items: center;
    }
    
    .nav-group {
      display: flex;
      gap: 0.25rem;
      padding: 0.25rem;
      background: var(--bg-secondary);
      border-radius: var(--radius);
    }

    .nav-link {
      padding: 0.5rem 0.875rem;
      border-radius: var(--radius);
      color: var(--text-secondary);
      text-decoration: none;
      font-weight: 500;
      font-size: 0.9375rem;
      transition: all 0.2s ease;
      white-space: nowrap;

      &:hover {
        background: var(--bg-tertiary);
        color: var(--text-primary);
      }

      &.active {
        background: var(--primary);
        color: white;
      }
      
      &.admin-link {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        margin-left: 0.5rem;
        
        &:hover {
          opacity: 0.9;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }
    }

    .user-menu {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }

    .user-name {
      color: var(--text-secondary);
      font-weight: 500;
    }
    
    .role-badge {
      display: inline-block;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .role-user {
      background: #3b82f6;
      color: white;
    }
    
    .role-admin {
      background: #8b5cf6;
      color: white;
    }
    
    .role-superadmin {
      background: linear-gradient(135deg, #f59e0b 0%, #ef4444 100%);
      color: white;
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
  Role = Role;  // Expose Role enum to template
}
