import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { ApiResponse } from '../models/common.models';
import { AccessFeedbackService } from './access-feedback.service';
import {
  buildLogoutHeaders,
  buildSessionUser,
  clearStoredSession,
  persistSession,
  readStoredUser,
} from './auth-session.utils';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly tokenStorageKey = 'hms_token';
  private readonly userStorageKey = 'hms_user';

  private currentUserSubject = new BehaviorSubject<User | null>(readStoredUser(this.userStorageKey));
  public currentUser$ = this.currentUserSubject.asObservable();

  public get currentUser(): User | null {
    return this.currentUserSubject.value;
  }

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService,
  ) {
    if (!this.currentUserSubject.value || !this.getToken()) {
      this.clearSession();
    }
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((res) => {
        if (res.success && res.data) {
          const user = buildSessionUser(res.data);
          this.currentUserSubject.next(user);
          persistSession(this.tokenStorageKey, this.userStorageKey, res.data.token, user);
        }
      }),
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${environment.apiUrl}/auth/register`, request);
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
    return sessionStorage.getItem(this.tokenStorageKey);
  }

  getUserRole(): string | null {
    return this.currentUserSubject.value?.role || null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken() && !!this.currentUserSubject.value;
  }

  isPasswordChangeRequired(): boolean {
    return !!this.currentUserSubject.value?.passwordChangeRequired;
  }

  markPasswordChanged(): void {
    const user = this.currentUserSubject.value;
    if (user) {
      user.passwordChangeRequired = false;
      this.currentUserSubject.next(user);
      sessionStorage.setItem(this.userStorageKey, JSON.stringify(user));
    }
  }

  private clearSession(): void {
    clearStoredSession(this.tokenStorageKey, this.userStorageKey);
    this.currentUserSubject.next(null);
    this.accessFeedbackService.close();
  }
}
