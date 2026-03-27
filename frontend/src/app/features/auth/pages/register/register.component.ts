import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { Role } from '../../../../core/models/auth.models';
import { AuthService } from '../../../../core/services/auth.service';
import { buildRoleOptions, createRegisterForm, markFormControlsTouched } from '../../auth-form.utils';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, DropdownModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  readonly roles = Object.values(Role);

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService,
  ) {
    this.registerForm = createRegisterForm(this.fb);
  }

  roleOptions(): Array<{ label: string; value: Role }> {
    return buildRoleOptions(this.roles);
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/login'], { replaceUrl: true });
        },
        error: (err: HttpErrorResponse) => {
          this.isLoading = false;
          this.errorMessage = err.error?.message || 'Registration failed.';
        },
      });
    } else {
      this.errorMessage = 'Please fill all required fields before registering.';
      markFormControlsTouched(this.registerForm);
    }
  }
}
