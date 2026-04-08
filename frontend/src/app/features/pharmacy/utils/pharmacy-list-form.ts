import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CODE_PATTERN, trimRequired } from '../../../core/validators/app-validators';

export function futureDateValidator(control: AbstractControl): { notFuture: true } | null {
  if (!control.value) {
    return null;
  }

  const date = new Date(control.value);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return date > today ? null : { notFuture: true };
}

export function createMedicineForm(fb: FormBuilder): FormGroup {
  return fb.group({
    name: ['', [...trimRequired(2, 200)]],
    medicineCode: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(CODE_PATTERN)]],
    category: ['', Validators.required],
    manufacturer: ['', [...trimRequired(2, 100)]],
    description: ['', Validators.maxLength(500)],
    unitPrice: [null, [Validators.required, Validators.min(0.01)]],
    quantityInStock: [0, [Validators.required, Validators.min(0)]],
    reorderLevel: [10, [Validators.required, Validators.min(0)]],
    expiryDate: [null, [futureDateValidator]],
  });
}

export function createRestockForm(fb: FormBuilder): FormGroup {
  return fb.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
  });
}




