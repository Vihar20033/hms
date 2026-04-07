import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/auth.models';
import { AccessFeedbackService } from '../services/access-feedback.service';
import { AuthService } from '../services/auth.service';
import { homeRouteForRole } from '../constants/role-route-map';

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
    const fallbackRoute = homeRouteForRole(user?.role);
    accessFeedbackService.showUnauthorized(`You do not have access to ${state.url}. Redirecting you to your workspace.`);
    return router.createUrlTree([fallbackRoute]);
  }

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
