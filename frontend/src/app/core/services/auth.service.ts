import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { ApiResponse } from '../models/common.models';
import { AccessFeedbackService } from './access-feedback.service';
import {
  buildLogoutHeaders,   // Build logout headers
  buildSessionUser,     // Build user from auth response
  clearStoredSession, // Clear user from session storage
  persistSession,   // Save user to session storage
  readStoredUser, // Get user from session storage
} from './auth-session.utils';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly tokenKey = 'hms_token';
  private readonly refreshKey = 'hms_refresh_token';
  private readonly userKey = 'hms_user';

  private currentUserSubject = new BehaviorSubject<User | null>(readStoredUser(this.userKey));
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService,
  ) {
    if (!this.currentUserValue || !this.getToken()) {
      this.clearSession();
    }
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  get currentUser(): User | null {
    return this.currentUserValue;
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((res) => {
        if (res.success && res.data) {
          this.handleAuthSuccess(res.data);
        }
      }),
    );
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/refresh`, {
        refreshToken,
      })
      .pipe(
        tap((res) => {
          if (res.success && res.data) {
            this.handleAuthSuccess(res.data);
          }
        }),
      );
  }

  register(request: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>
    (`${environment.apiUrl}/auth/register`, request);
  }

  logout(): void {
    const token = this.getToken();
    if (token) {
      this.http
        .post(
          `${environment.apiUrl}/auth/logout`,
          {},
          {
            headers: buildLogoutHeaders(token),
            responseType: 'text',
          },
        )
        .subscribe({
          next: () => this.clearSession(),
          error: () => this.clearSession(),
        });
    } else {
      this.clearSession();
    }
  }

  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http.post(`${environment.apiUrl}/auth/change-password`, request, { responseType: 'text' });
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  getRefreshToken(): string | null {
    return sessionStorage.getItem(this.refreshKey);
  }

  getUserRole(): string | null {
    return this.currentUserValue?.role || null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken() && !!this.currentUserValue;
  }

  isPasswordChangeRequired(): boolean {
    return !!this.currentUserValue?.passwordChangeRequired;
  }

  markPasswordChanged(): void {
    const user = this.currentUserValue;
    if (user) {
      user.passwordChangeRequired = false;
      this.currentUserSubject.next({ ...user });
      sessionStorage.setItem(this.userKey, JSON.stringify(user));
    }
  }

  private handleAuthSuccess(data: AuthResponse): void {
    const user = buildSessionUser(data);
    this.currentUserSubject.next(user);
    persistSession(this.tokenKey, this.refreshKey, this.userKey, data.token, data.refreshToken, user);
  }

  private clearSession(): void {
    clearStoredSession(this.tokenKey, this.refreshKey, this.userKey);
    this.currentUserSubject.next(null);
    this.accessFeedbackService.close();
  }
}
