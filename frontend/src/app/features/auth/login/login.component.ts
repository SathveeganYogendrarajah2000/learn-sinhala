import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <h1>Welcome Back</h1>
        <p class="subtitle">Sign in to continue learning Sinhala</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="form-label" for="email">Email</label>
            <input
              id="email"
              type="email"
              class="form-input"
              formControlName="email"
              [class.error]="form.get('email')?.invalid && form.get('email')?.touched"
            />
            @if (form.get('email')?.hasError('required') && form.get('email')?.touched) {
              <span class="form-error">Email is required</span>
            }
            @if (form.get('email')?.hasError('email') && form.get('email')?.touched) {
              <span class="form-error">Please enter a valid email</span>
            }
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Password</label>
            <input
              id="password"
              type="password"
              class="form-input"
              formControlName="password"
              [class.error]="form.get('password')?.invalid && form.get('password')?.touched"
            />
            @if (form.get('password')?.hasError('required') && form.get('password')?.touched) {
              <span class="form-error">Password is required</span>
            }
          </div>

          @if (errorMessage) {
            <div class="error-banner">{{ errorMessage }}</div>
          }

          <button
            type="submit"
            class="btn btn-primary btn-block"
            [disabled]="form.invalid || loading"
          >
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <p class="auth-footer">
          Don't have an account? <a routerLink="/register">Sign up</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }

    .auth-card {
      width: 100%;
      max-width: 400px;
    }

    h1 {
      margin-bottom: 0.5rem;
      text-align: center;
    }

    .subtitle {
      text-align: center;
      color: var(--text-secondary);
      margin-bottom: 2rem;
    }

    .btn-block {
      width: 100%;
      margin-top: 1rem;
    }

    .error-banner {
      background: #fef2f2;
      color: var(--danger);
      padding: 0.75rem 1rem;
      border-radius: var(--radius);
      margin-bottom: 1rem;
      font-size: 0.875rem;
    }

    .auth-footer {
      text-align: center;
      margin-top: 1.5rem;
      color: var(--text-secondary);
    }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  loading = false;
  errorMessage = '';

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.message || 'Login failed. Please try again.';
      }
    });
  }
}
