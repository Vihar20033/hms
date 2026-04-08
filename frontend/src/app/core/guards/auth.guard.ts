import { AuthService } from '../../features/auth/services/auth.service';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { inject } from '@angular/core';
import { of } from 'rxjs';
import { User } from '../../features/auth/models/auth.models';

export const authGuard: CanActivateFn = (route, state) => {

  const router = inject(Router);
  const authService = inject(AuthService);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], 
      { queryParams: { returnUrl: state.url } });
  }

  // If routes required specific roles, check if the user has one of those roles
  const requiredRoles = route.data?.['roles'] as string[];

  if (requiredRoles && requiredRoles.length > 0) {
    
    const userRole = authService.getUserRole();
    
    if (!userRole || !requiredRoles.includes(userRole)) {
      return router.createUrlTree(['/unauthorized']);
    }
  }

  return true;
};






