import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { StorageService } from '@core/services/storage.service';

/**
 * HTTP interceptor that adds JWT token to outgoing requests.
 *
 * Adds Authorization header: "Bearer <token>"
 * Skips requests to external URLs.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(StorageService);
  const token = storage.getToken();

  // Skip if no token or external URL
  if (!token || !req.url.includes('/api')) {
    return next(req);
  }

  // Clone request with auth header
  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  return next(authReq);
};
