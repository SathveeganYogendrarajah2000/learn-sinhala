import { Injectable, computed, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '@env/environment';
import { StorageService } from './storage.service';
import { NotificationService } from './notification.service';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User
} from '@core/models/auth.model';

/**
 * Authentication service using Angular signals for state management.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private notification = inject(NotificationService);

  // Reactive state with signals
  private currentUserSignal = signal<User | null>(null);
  private loadingSignal = signal<boolean>(false);

  // Public computed values
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.currentUserSignal());
  readonly isLoading = this.loadingSignal.asReadonly();

  constructor(
    private http: HttpClient,
    private storage: StorageService,
    private router: Router
  ) {
    // Restore user from storage on init
    this.restoreSession();
  }

  /**
   * Register a new user.
   */
  register(request: RegisterRequest): Observable<AuthResponse> {
    this.loadingSignal.set(true);

    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(
        tap({
          next: (response) => this.handleAuthSuccess(response),
          error: () => this.loadingSignal.set(false),
          complete: () => this.loadingSignal.set(false)
        })
      );
  }

  /**
   * Login with credentials.
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    this.loadingSignal.set(true);

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap({
          next: (response) => this.handleAuthSuccess(response),
          error: () => this.loadingSignal.set(false),
          complete: () => this.loadingSignal.set(false)
        })
      );
  }

  /**
   * Logout and clear session.
   */
  logout(): void {
    this.storage.clear();
    this.currentUserSignal.set(null);
    this.notification.info('You have been logged out');
    this.router.navigate(['/login']);
  }

  /**
   * Get current user profile from API.
   */
  fetchCurrentUser(): Observable<User> {
    return this.http.get<User>(`${environment.apiUrl}/users/me`)
      .pipe(
        tap(user => {
          this.currentUserSignal.set(user);
          this.storage.setUser(user);
        })
      );
  }

  /**
   * Check if user has valid token.
   */
  hasValidToken(): boolean {
    return this.storage.hasToken();
  }

  /**
   * Get token for HTTP requests.
   */
  getToken(): string | null {
    return this.storage.getToken();
  }

  /**
   * Handle successful authentication.
   */
  private handleAuthSuccess(response: AuthResponse): void {
    this.storage.setToken(response.token);

    // Create basic user from response
    const user: Partial<User> = {
      username: response.username,
      displayName: response.displayName
    };
    this.storage.setUser(user);
    this.currentUserSignal.set(user as User);

    this.notification.success(`Welcome, ${response.displayName || response.username}!`);

    // Fetch full user profile
    this.fetchCurrentUser().subscribe();
  }

  /**
   * Restore session from storage.
   */
  private restoreSession(): void {
    if (this.storage.hasToken()) {
      const user = this.storage.getUser<User>();
      if (user) {
        this.currentUserSignal.set(user);
      }
    }
  }
}
