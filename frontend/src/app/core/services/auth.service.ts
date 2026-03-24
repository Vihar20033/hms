import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { BehaviorSubject, Observable, timer } from 'rxjs';
import { retry, tap, timeout } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { ApiResponse } from '../models/common.models';
import { AccessFeedbackService } from './access-feedback.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly tokenStorageKey = 'hms_token';
  private readonly userStorageKey = 'hms_user';
  
  // High-performance state management using Signals
  private currentUserSignal = signal<User | null>(this.getUserFromStorage());
  public currentUser = this.currentUserSignal.asReadonly();
  public isAuthenticated = computed(() => !!this.currentUser());

  // Compatibility layer for RxJS users
  private currentUserSubject = new BehaviorSubject<User | null>(this.currentUserSignal());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService,
  ) {
    this.clearLegacyLocalStorage();

    if (!this.currentUserSignal() || !this.getToken()) {
      this.clearSession();
    }
  }

  public get currentUserValue(): User | null {
    return this.currentUserSignal();
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, request).pipe(
      retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }),
      timeout(10000),
      tap((res) => {
        if (res.success && res.data) {
          this.clearAuthArtifacts();
          sessionStorage.setItem(this.tokenStorageKey, res.data.token);
          const user: User = {
            username: res.data.username,
            email: res.data.email,
            role: res.data.role,
            passwordChangeRequired: res.data.passwordChangeRequired,
          };
          this.currentUserSignal.set(user);
          this.currentUserSubject.next(user);
          sessionStorage.setItem(this.userStorageKey, JSON.stringify(user));
        }
      }),
    );
  }

  register(request: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http
      .post<ApiResponse<string>>(`${environment.apiUrl}/auth/register`, request)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  logout(): void {
    const token = this.getToken();

    if (token) {
      this.http
        .post(
          `${environment.apiUrl}/auth/logout`,
          {},
          { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }), responseType: 'text' },
        )
        .subscribe({
          error: () => {
            // Client-side logout still proceeds even if token revocation request fails.
          },
        });
    }

    this.clearSession();
  }

  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http
      .post(`${environment.apiUrl}/auth/change-password`, request, { responseType: 'text' })
      .pipe(retry({ count: 1, delay: () => timer(500) }), timeout(10000));
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenStorageKey);
  }

  getUserRole(): string | null {
    return this.currentUserSignal()?.role || null;
  }

  isPasswordChangeRequired(): boolean {
    return !!this.currentUserSignal()?.passwordChangeRequired;
  }

  markPasswordChanged(): void {
    const currentUser = this.currentUserSignal();
    if (!currentUser) {
      return;
    }

    const updatedUser = { ...currentUser, passwordChangeRequired: false };
    this.currentUserSignal.set(updatedUser);
    this.currentUserSubject.next(updatedUser);
    sessionStorage.setItem(this.userStorageKey, JSON.stringify(updatedUser));
  }

  private getUserFromStorage(): User | null {
    const user = sessionStorage.getItem(this.userStorageKey);

    if (!user) {
      return null;
    }

    try {
      return JSON.parse(user) as User;
    } catch {
      this.clearSession();
      return null;
    }
  }

  private clearSession(): void {
    this.clearAuthArtifacts();
    this.currentUserSignal.set(null);
    this.currentUserSubject.next(null);
  }

  private clearLegacyLocalStorage(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  private clearAuthArtifacts(): void {
    sessionStorage.removeItem(this.tokenStorageKey);
    sessionStorage.removeItem(this.userStorageKey);
    this.accessFeedbackService.close();
  }
}

