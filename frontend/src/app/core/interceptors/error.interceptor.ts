import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { homeRouteForRole } from '../constants/role-route-map';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const isAuthRequest =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/refresh') ||
    req.url.includes('/auth/logout');

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthRequest) {
        authService.logout(false).subscribe({
          complete: () => {
            router.navigate(['/login']);
          },
        });
        return throwError(() => err);
      }

      // Log a human-readable message to the console for debugging
      let errorMessage = 'An unknown error occurred';

      if (err.error instanceof ErrorEvent) {
        errorMessage = `Network error: ${err.error.message}`;
        console.error('Network error:', err.error);
      } else {
        switch (err.status) {
          case 0:
            errorMessage = 'Unable to connect to server. Please check your internet connection.';
            break;
          case 401:
            errorMessage = 'Please log in again.';
            break;
          case 403:
            router.navigate([homeRouteForRole(authService.currentUser?.role)]);
            errorMessage = 'You do not have permission to perform this action.';
            break;
          case 404:
            errorMessage = err.error?.message || 'Resource not found.';
            break;
          case 422:
            errorMessage = err.error?.message || 'Please check your input.';
            break;
          case 429:
            errorMessage = err.error?.message || 'Too many requests. Please wait a moment and try again.';
            break;
          case 409:
            errorMessage = err.error?.message || 'This record has been modified by another user. Please refresh and try again.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
          default:
            errorMessage = err.error?.message || err.statusText || `HTTP error ${err.status}`;
        }
      }

      console.error(`[HMS Error ${err.status}]:`, errorMessage);

      // IMPORTANT: Re-throw the original HttpErrorResponse — NOT a plain string.
      // Throwing a string destroys err.status and err.error, which breaks any
      // component-level error handler that needs to inspect those fields
      // (e.g., the doctor reassignment modal checks err.status === 400).
      return throwError(() => err);
    }),
  );
};
