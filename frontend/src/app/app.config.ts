import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { AuthService } from './core/services/auth.service';

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
