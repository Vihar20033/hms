import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { AuthService } from './features/auth/services/auth.service';
import { DatePipe, DecimalPipe } from '@angular/common';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, Routes } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { routes } from './app.routes';

function initializeAuth(authService: AuthService) {
  return () => authService.initializeSession();
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideAnimationsAsync(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor, errorInterceptor])),
    {
      provide: APP_INITIALIZER,
      multi: true,
      useFactory: initializeAuth,
      deps: [AuthService],
    },
    DatePipe,
    DecimalPipe
  ],
};






