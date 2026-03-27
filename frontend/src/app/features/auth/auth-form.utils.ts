import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Role } from '../../core/models/auth.models';
import { USERNAME_PATTERN, differentFieldsValidator, matchFieldsValidator } from '../../core/validators/app-validators';

export function createLoginForm(fb: FormBuilder): FormGroup {
  return fb.group({
    username: [
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(USERNAME_PATTERN)],
    ],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
  });
}

export function createRegisterForm(fb: FormBuilder): FormGroup {
  return fb.group({
    username: [
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(USERNAME_PATTERN)],
    ],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    role: [Role.RECEPTIONIST, [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
  });
}

export function createChangePasswordForm(fb: FormBuilder): FormGroup {
  return fb.group(
    {
      currentPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
      newPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(128)]],
    },
    {
      validators: [
        matchFieldsValidator('newPassword', 'confirmPassword'),
        differentFieldsValidator('currentPassword', 'newPassword', 'sameAsCurrent'),
      ],
    },
  );
}

export function markFormControlsTouched(form: FormGroup): void {
  Object.keys(form.controls).forEach((key) => {
    form.controls[key].markAsTouched();
  });
}

export function buildRoleOptions(roles: Role[]): Array<{ label: string; value: Role }> {
  return roles.map((role) => ({
    label: role.replace(/_/g, ' '),
    value: role,
  }));
}
