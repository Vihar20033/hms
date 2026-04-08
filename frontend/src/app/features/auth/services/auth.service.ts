import { AccessFeedbackService } from '../../../shared/services/access-feedback.service';
import { ApiResponse } from '../../../core/models/common.models';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, Role, User } from '../models/auth.models';
import { BehaviorSubject, firstValueFrom, Observable, of } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable, Pipe } from '@angular/core';
import { buildSessionUser } from '../../../core/utils/auth-session.utils';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private readonly authUrl = `${environment.apiUrl}/auth`;

  private readonly currentUserSubject = new BehaviorSubject<User | null>(null);
  readonly currentUser$ = this.currentUserSubject.asObservable();
  private accessToken: string | null = null;
  private restorePromise: Promise<void> | null = null;

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService
  ) {}

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  get currentUser(): User | null {
    return this.currentUserValue;
  }

  getAccessToken(): string | null {
    return this.accessToken;
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>
    (`${this.authUrl}/login`, request, this.cookieOptions()).pipe(
      tap((res: ApiResponse<AuthResponse>) => {
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
        tap((res: ApiResponse<AuthResponse>) => {
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

    if (!this.restorePromise) {
      this.restorePromise = firstValueFrom(
        this.refreshToken().pipe(
          map(() => undefined),
          catchError(() => {
            this.clearUserState();
            return of(undefined);
          }),
          finalize(() => {
            this.restorePromise = null;
          }),
        ),
      );
    }
    return this.restorePromise as Promise<void>;
  }

  register(request: RegisterRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>
    (`${this.authUrl}/register`, request);
  }

  logout(notifyServer: boolean = true): Observable<void> {
    if (!notifyServer) {
      this.clearUserState();
      return of(undefined);
    }

    return this.http
      .post(`${this.authUrl}/logout`, {}, 
        { ...this.cookieOptions(), responseType: 'text' })
      .pipe(
        map(() => undefined),
        finalize(() => this.clearUserState()),
      );
  }

  changePassword(request: ChangePasswordRequest): Observable<string> {
    return this.http.post(`${this.authUrl}/change-password`, request, {
      ...this.cookieOptions(),
      responseType: 'text',
    }) as Observable<string>;
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






