import { HttpHeaders } from '@angular/common/http';
import { AuthResponse, User } from '../models/auth.models';

export function buildSessionUser(auth: AuthResponse): User {
  return {
    username: auth.username,
    email: auth.email,
    role: auth.role,
    passwordChangeRequired: auth.passwordChangeRequired,
  };
}

export function readStoredUser(storageKey: string): User | null {
  const user = sessionStorage.getItem(storageKey);
  if (!user) {
    return null;
  }

  try {
    return JSON.parse(user) as User;
  } catch {
    return null;
  }
}

export function persistSession(
  tokenKey: string,
  refreshKey: string,
  userKey: string,
  token: string,
  refreshToken: string,
  user: User,
): void { 
  sessionStorage.setItem(tokenKey, token);
  sessionStorage.setItem(refreshKey, refreshToken);
  sessionStorage.setItem(userKey, JSON.stringify(user));
}

export function clearStoredSession(tokenKey: string, refreshKey: string, userKey: string): void {
  sessionStorage.removeItem(tokenKey);
  sessionStorage.removeItem(refreshKey);
  sessionStorage.removeItem(userKey);
}

export function buildLogoutHeaders(token: string): HttpHeaders {
  return new HttpHeaders({ Authorization: `Bearer ${token}` });
}
