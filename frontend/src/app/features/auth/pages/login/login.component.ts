import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { canRoleAccessPath, homeRouteForRole } from '../../../../core/constants/role-route-map';
import { AuthService } from '../../../../core/services/auth.service';
import { createLoginForm, markFormControlsTouched } from '../../auth-form.utils';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private route: ActivatedRoute,
  ) {
    this.loginForm = createLoginForm(this.fb);
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.login(this.loginForm.value).subscribe({
        next: (response) => {
          this.isLoading = false;
          if (response.data && response.data.passwordChangeRequired) {
            this.router.navigate(['/change-password'], { replaceUrl: true });
            return;
          }
          const defaultUrl = homeRouteForRole(response.data.role);
          const requestedReturnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          const returnUrl = requestedReturnUrl
            ? (canRoleAccessPath(response.data.role, requestedReturnUrl) ? requestedReturnUrl : defaultUrl)
            : (requestedReturnUrl || defaultUrl);
          this.router.navigateByUrl(returnUrl, { replaceUrl: true });
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || err.message || 'Login failed. Please check your credentials.';
        },
      });
    } else {
      markFormControlsTouched(this.loginForm);
    }
  }
}
