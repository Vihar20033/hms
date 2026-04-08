import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { homeRouteForRole } from '../../../../core/constants/role-route-map';
import { HttpErrorResponse } from '@angular/common/http';
import { Role } from '../../models/auth.models';
import { Router } from '@angular/router';
import { createChangePasswordForm } from '../../auth-form.utils';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.scss',
})
export class ChangePasswordComponent {
  passwordForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
  ) {
    this.passwordForm = createChangePasswordForm(this.fb);
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { currentPassword, newPassword } = this.passwordForm.value;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.authService.markPasswordChanged();
        this.successMessage = 'Password updated successfully. Redirecting to your workspace...';
        this.isLoading = false;
        setTimeout(() => {
          this.router.navigate([homeRouteForRole(this.authService.currentUser?.role)], { replaceUrl: true });
        }, 1200);
      },
      error: (err: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Unable to update password.';
      },
    });
  }
}









