import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LayoutComponent } from '@shared/components/layout/layout.component';
import { LoadingComponent } from '@shared/components/loading/loading.component';
import { ApiService } from '@core/services/api.service';
import { AuthService } from '@core/services/auth.service';
import { SessionStats } from '@core/models/practice.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [LayoutComponent, LoadingComponent, RouterLink],
  template: `
    <app-layout>
      <div class="container">
        <header class="dashboard-header">
          <h1>Hello, {{ auth.currentUser()?.displayName || 'Learner' }}!</h1>
          <p class="subtitle">Ready to learn some Sinhala today?</p>
        </header>

        @if (loading()) {
          <app-loading message="Loading your progress..." />
        } @else {
          <div class="stats-grid">
            <div class="stat-card card">
              <span class="stat-value">{{ stats()?.completedToday || 0 }}</span>
              <span class="stat-label">Practiced Today</span>
              <span class="stat-goal">Goal: {{ stats()?.dailyGoal || 10 }}</span>
            </div>

            <div class="stat-card card">
              <span class="stat-value">{{ stats()?.mastered || 0 }}</span>
              <span class="stat-label">Words Mastered</span>
            </div>

            <div class="stat-card card">
              <span class="stat-value">{{ stats()?.totalLearned || 0 }}</span>
              <span class="stat-label">Total Learned</span>
            </div>

            <div class="stat-card card">
              <span class="stat-value">{{ stats()?.inProgress || 0 }}</span>
              <span class="stat-label">In Progress</span>
            </div>
          </div>

          <div class="actions">
            <a routerLink="/practice" class="btn btn-primary btn-lg">
              Start Practice Session
            </a>
            <a routerLink="/vocabulary" class="btn btn-secondary btn-lg">
              Browse Vocabulary
            </a>
          </div>
        }
      </div>
    </app-layout>
  `,
  styles: [`
    .dashboard-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .subtitle {
      color: var(--text-secondary);
      margin-top: 0.5rem;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      text-align: center;
      padding: 1.5rem;
    }

    .stat-value {
      display: block;
      font-size: 2.5rem;
      font-weight: 700;
      color: var(--primary);
    }

    .stat-label {
      display: block;
      color: var(--text-secondary);
      margin-top: 0.5rem;
    }

    .stat-goal {
      display: block;
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }

    .actions {
      display: flex;
      justify-content: center;
      gap: 1rem;
      flex-wrap: wrap;
    }

    .btn-lg {
      padding: 1rem 2rem;
      font-size: 1.125rem;
    }
  `]
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private api = inject(ApiService);

  loading = signal(true);
  stats = signal<SessionStats | null>(null);

  ngOnInit(): void {
    this.loadStats();
  }

  private loadStats(): void {
    this.api.getTodaysPractice().subscribe({
      next: (session) => {
        this.stats.set(session.stats);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }
}
