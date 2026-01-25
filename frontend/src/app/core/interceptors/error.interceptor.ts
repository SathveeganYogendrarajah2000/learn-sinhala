import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { StorageService } from '@core/services/storage.service';
import { NotificationService } from '@core/services/notification.service';
import { environment } from '@env/environment';
import { ApiError } from '@core/models/api.model';

/**
 * HTTP interceptor for handling errors globally.
 *
 * - 401 Unauthorized: Clear token and redirect to login
 * - 403 Forbidden: Show message and redirect to dashboard
 * - 404 Not Found: Show not found message
 * - 500+ Server Error: Show server error message
 * - Network Error: Show connection error message
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const storage = inject(StorageService);
  const notification = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let userMessage = 'An unexpected error occurred';

      // Handle different error types
      if (error.status === 0) {
        // Network error or CORS issue
        userMessage = 'Unable to connect to server. Please check your connection.';
        notification.error(userMessage);
      } else if (error.status === 401) {
        // Token expired or invalid
        userMessage = 'Session expired. Please log in again.';
        storage.clear();
        notification.warning(userMessage);
        router.navigate(['/login'], {
          queryParams: { returnUrl: router.url }
        });
      } else if (error.status === 403) {
        userMessage = 'You do not have permission to perform this action.';
        notification.error(userMessage);
        router.navigate(['/dashboard']);
      } else if (error.status === 404) {
        userMessage = 'The requested resource was not found.';
        notification.error(userMessage);
      } else if (error.status === 422) {
        // Validation error - show specific message from server
        userMessage = error.error?.message || 'Validation error. Please check your input.';
        notification.error(userMessage);
      } else if (error.status >= 500) {
        userMessage = 'Server error. Please try again later.';
        notification.error(userMessage);
      } else {
        // Other errors - use server message if available
        userMessage = error.error?.message || error.message || userMessage;
        notification.error(userMessage);
      }

      // Log in development
      if (environment.debug.logErrors) {
        console.error('API Error:', {
          status: error.status,
          message: userMessage,
          url: req.url,
          error: error.error
        });
      }

      // Create standardized error object
      const apiError: ApiError = {
        status: error.status,
        message: userMessage,
        error: error.error,
        timestamp: new Date().toISOString(),
        path: req.url
      };

      return throwError(() => apiError);
    })
  );
};
