import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  LICENSE_NUMBER_PATTERN,
  PERSON_NAME_PATTERN,
  PHONE_PATTERN,
  STRONG_PASSWORD_PATTERN,
  USERNAME_PATTERN,
  trimRequired,
} from '../../../../core/validators/app-validators';

export function createDoctorRegistrationForm(fb: FormBuilder): FormGroup {
  return fb.group({
    username: [
      '',
      [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(USERNAME_PATTERN)],
    ],
    temporaryPassword: [
      'Welcome@123',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(128),
        Validators.pattern(STRONG_PASSWORD_PATTERN),
      ],
    ],
    firstName: ['', [...trimRequired(1, 50), Validators.pattern(PERSON_NAME_PATTERN)]],
    lastName: ['', [...trimRequired(1, 50), Validators.pattern(PERSON_NAME_PATTERN)]],
    specialization: ['', [...trimRequired(2, 100)]],
    department: ['', Validators.required],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    phoneNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
    consultationFee: [null, [Validators.required, Validators.min(0.01)]],
    licenseNumber: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(LICENSE_NUMBER_PATTERN)]],
    qualification: ['', [Validators.maxLength(200)]],
    experienceYears: [0, [Validators.min(0), Validators.max(100)]],
    designation: ['', [Validators.maxLength(100)]],
    bio: ['', [Validators.maxLength(1000)]],
  });
}
