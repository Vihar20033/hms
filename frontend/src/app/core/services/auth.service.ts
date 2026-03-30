import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, ChangePasswordRequest, LoginRequest, RegisterRequest, User } from '../models/auth.models';
import { ApiResponse } from '../models/common.models';
import { AccessFeedbackService } from './access-feedback.service';
import { CookieService } from './cookie.service';
import { UserService } from './user.service';
import {
  buildSessionUser,
  clearUser,
  persistUser,
  readStoredUser,
} from './auth-session.utils';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly userKey = 'hms_user';
  private readonly loggedInKey = 'hms_logged_in';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private accessFeedbackService: AccessFeedbackService,
    private cookieService: CookieService,
    private userService: UserService
  ) {
    const storedUser = readStoredUser(this.cookieService.get(this.userKey));
    if (storedUser) {
      this.currentUserSubject.next(storedUser);
      return;
    }

    if (this.cookieService.get(this.loggedInKey) === 'true') {
      this.checkUserStatus();
      return;
    }

    this.clearSession();
  }

  get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  get currentUser(): User | null {
    return this.currentUserValue;
  }

  login(request: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/login`, request, {
      withCredentials: true,
    }).pipe(
      tap((res) => {
        if (res.success && res.data) {
          this.handleAuthSuccess(res.data);
        }
      }),
    );
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    return this.http
      .post<ApiResponse<AuthResponse>>(`${environment.apiUrl}/auth/refresh`, {}, {
        withCredentials: true,
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
    const user = this.currentUserValue;
    if (user) {
      this.http
        .post(
          `${environment.apiUrl}/auth/logout`,
          {},
          {
            withCredentials: true,
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
    return this.http.post(`${environment.apiUrl}/auth/change-password`, request, { 
      withCredentials: true,
      responseType: 'text' 
    });
  }

  getToken(): string | null {
    return null;
  }

  getRefreshToken(): string | null {
    return null;
  }

  getUserRole(): string | null {
    return this.currentUserValue?.role || null;
  }

  isAuthenticated(): boolean {
    return !!this.currentUserValue;
  }

  isPasswordChangeRequired(): boolean {
    return !!this.currentUserValue?.passwordChangeRequired;
  }

  markPasswordChanged(): void {
    const user = this.currentUserValue;
    if (user) {
      this.persistCurrentUser({
        ...user,
        passwordChangeRequired: false,
      });
    }
  }

  private handleAuthSuccess(data: AuthResponse): void {
    this.persistCurrentUser(buildSessionUser(data));
  }

  private checkUserStatus(): void {
    this.userService.getCurrentUser().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.persistCurrentUser(buildSessionUser(res.data as AuthResponse));
          return;
        }

        this.clearSession();
      },
      error: () => this.clearSession(),
    });
  }

  private persistCurrentUser(user: User): void {
    this.currentUserSubject.next(user);
    this.cookieService.set(this.userKey, persistUser(user));
    this.cookieService.set(this.loggedInKey, 'true');
  }

  private clearSession(): void {
    clearUser();
    this.cookieService.delete(this.userKey);
    this.cookieService.delete(this.loggedInKey);
    this.currentUserSubject.next(null);
    this.accessFeedbackService.close();
  }

}
