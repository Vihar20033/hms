import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/auth.models';
import { AccessFeedbackService } from '../services/access-feedback.service';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const accessFeedbackService = inject(AccessFeedbackService);
  const user = authService.currentUser;

  const allowedRoles = route.data?.['roles'] as Array<Role>;

  if (!allowedRoles || allowedRoles.length === 0) {
    return true;
  }

  if (user && allowedRoles.includes(user.role)) {
    return true;
  }

  if (authService.isAuthenticated()) {
    accessFeedbackService.showUnauthorized();
    return router.createUrlTree(['/dashboard']);
  }

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
