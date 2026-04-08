import { AuthResponse, Role, User } from '../../features/auth/models/auth.models';

export function buildSessionUser(auth: AuthResponse): User {
  return {
    username: auth.username,
    email: auth.email,
    role: auth.role,
    passwordChangeRequired: auth.passwordChangeRequired,
  };
}






