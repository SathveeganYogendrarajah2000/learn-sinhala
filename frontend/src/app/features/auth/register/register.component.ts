import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { RegisterRequest } from '@core/models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="auth-page">
      <div class="auth-card card">
        <h1>Create Account</h1>
        <p class="subtitle">Start your Sinhala learning journey</p>

        <form [formGroup]="form" (ngSubmit)="onSubmit()">
          <div class="row">
            <div class="col">
              <div class="form-group">
                <label class="form-label" for="firstName">First Name</label>
                <input
                  id="firstName"
                  type="text"
                  class="form-input"
                  formControlName="firstName"
                  [class.error]="form.get('firstName')?.invalid && form.get('firstName')?.touched"
                />
              </div>
            </div>
            <div class="col">
              <div class="form-group">
                <label class="form-label" for="lastName">Last Name</label>
                <input
                  id="lastName"
                  type="text"
                  class="form-input"
                  formControlName="lastName"
                  [class.error]="form.get('lastName')?.invalid && form.get('lastName')?.touched"
                />
              </div>
            </div>
          </div>

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
            @if (form.get('password')?.hasError('minlength') && form.get('password')?.touched) {
              <span class="form-error">Password must be at least 6 characters</span>
            }
          </div>

          <div class="form-group">
            <label class="form-label" for="confirmPassword">Confirm Password</label>
            <input
              id="confirmPassword"
              type="password"
              class="form-input"
              formControlName="confirmPassword"
              [class.error]="form.get('confirmPassword')?.touched && form.hasError('mismatch')"
            />
            @if (form.hasError('mismatch') && form.get('confirmPassword')?.touched) {
              <span class="form-error">Passwords do not match</span>
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
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>

        <p class="auth-footer">
          Already have an account? <a routerLink="/login">Sign in</a>
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

    .row {
      display: flex;
      gap: 1rem;
    }
    
    .col {
      flex: 1;
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
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  loading = false;
  errorMessage = '';

  passwordMatchValidator(g: any) {
    return g.get('password').value === g.get('confirmPassword').value
       ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const { firstName, lastName, email, password } = this.form.getRawValue();
    const request: RegisterRequest = { firstName, lastName, email, password };

    this.auth.register(request).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.message || 'Registration failed. Please try again.';
      }
    });
  }
}
