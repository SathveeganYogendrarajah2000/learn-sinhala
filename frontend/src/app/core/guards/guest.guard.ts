import { inject } from '@angular/core';
import { Router, type CanActivateFn } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

/**
 * Guard that protects routes for guests only (login, register).
 * Redirects to dashboard if user is already authenticated.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasValidToken()) {
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};
