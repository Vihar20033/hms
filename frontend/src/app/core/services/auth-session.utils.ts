import { AuthResponse, User } from '../models/auth.models';

export function buildSessionUser(auth: AuthResponse): User {
  return {
    username: auth.username,
    email: auth.email,
    role: auth.role,
    passwordChangeRequired: auth.passwordChangeRequired,
  };
}

export function readStoredUser(serializedUser: string | null): User | null {
  if (!serializedUser) {
    return null;
  }

  try {
    return JSON.parse(serializedUser) as User;
  } catch {
    return null;
  }
}

export function persistUser(user: User): string {
  return JSON.stringify(user);
}

export function clearUser(): null {
  return null;
}
