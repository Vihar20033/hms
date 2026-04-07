import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Role } from '../../core/models/auth.models';
import { USERNAME_PATTERN, differentFieldsValidator, matchFieldsValidator } from '../../core/validators/app-validators';

export const PASSWORD_POLICY_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/;
const PASSWORD_VALIDATORS = [
  Validators.required,
  Validators.minLength(8),
  Validators.maxLength(128),
  Validators.pattern(PASSWORD_POLICY_PATTERN),
];

export function createLoginForm(fb: FormBuilder): FormGroup {
  return fb.group({
    username: [
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(USERNAME_PATTERN)],
    ],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]],
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
    password: ['', PASSWORD_VALIDATORS],
  });
}

export function createChangePasswordForm(fb: FormBuilder): FormGroup {
  return fb.group(
    {
      currentPassword: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(128)]],
      newPassword: ['', PASSWORD_VALIDATORS],
      confirmPassword: ['', PASSWORD_VALIDATORS],
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
