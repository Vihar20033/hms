import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, firstValueFrom, of } from 'rxjs';
import { catchError, finalize, mapTo, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { ApiResponse } from '../models/common.models';
import { AccessFeedbackService } from './access-feedback.service';
import { buildSessionUser } from '../utils/auth-session.utils';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly authUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();
  private accessToken: string | null = null;
  private restorePromise: Promise<void> | null = null; // Stores on going refresh token operation

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService
  ) {}

  // Getters for current user and access token
  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  // Expose current user as an observable for components to subscribe to
  get currentUser(): User | null {
    return this.currentUserValue;
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  // Tap used for side effects to store session on successful login or token refresh
  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>
    (`${this.authUrl}/login`, request, this.cookieOptions()).pipe(
      tap((res) => {
        if (res.success && res.data) {
          this.storeSession(res.data);
        }
      }),
    );
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>
      (`${this.authUrl}/refresh`, {}, this.cookieOptions())
      .pipe(
        tap((res) => {
          if (res.success && res.data) {
            this.storeSession(res.data);
          }
        }),
      );
  }

  initializeSession(): Promise<void> {
    if (this.currentUserValue) {
      return Promise.resolve();
    }

    // Problem 5 Request coalescing: Ensure only one refresh token request is made when multiple components call initializeSession simultaneously
    if (!this.restorePromise) {
      this.restorePromise = firstValueFrom(
        this.refreshToken().pipe(mapTo(void 0),catchError(() => {
            this.clearUserState();
            return of(void 0);
          }),
          finalize(() => {
            this.restorePromise = null;
          }),
        ),
      );
    }
    return this.restorePromise;
  }

  register(request: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>
    (`${this.authUrl}/register`, request);
  }

  logout(notifyServer: boolean = true): Observable<void> {
    if (!notifyServer) {
      this.clearUserState();
      return of(void 0);
    }

    return this.http
      .post(`${this.authUrl}/logout`, {}, 
        { ...this.cookieOptions(), responseType: 'text' })
      .pipe(
        mapTo(void 0),
        finalize(() => this.clearUserState()),
      );
  }

  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http.post(`${this.authUrl}/change-password`, request, {
      ...this.cookieOptions(),
      responseType: 'text',
    });
  }

  getUserRole(): string | null {
    return this.currentUserValue?.role || null;
  }

  isAuthenticated(): boolean {
    return !!this.currentUserValue && !!this.accessToken;
  }

  isPasswordChangeRequired(): boolean {
    return !!this.currentUserValue?.passwordChangeRequired;
  }

  markPasswordChanged(): void {
    const user = this.currentUserValue;
    if (user) {
      this.currentUserSubject.next({
        ...user,
        passwordChangeRequired: false,
      });
    }
  }

  private storeSession(auth: AuthResponse): void {
    if (!auth.token) {
      this.clearUserState();
      return;
    }

    this.accessToken = auth.token;
    this.currentUserSubject.next(buildSessionUser(auth));
  }

  private clearUserState(): void {
    this.accessToken = null;
    this.currentUserSubject.next(null);
    this.accessFeedbackService.close();
  }

  private cookieOptions(): { withCredentials: true } {
    return { withCredentials: true };
  }
}
