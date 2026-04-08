import { Appointment } from '../../appointments/models/appointment.models';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { futureOrTodayDateValidator, trimRequired } from '../../../core/validators/app-validators';

export function createBillingItemGroup(
  fb: FormBuilder,
  item?: Partial<{ itemName: string; quantity: number; unitPrice: number }>,
): FormGroup {
  return fb.group({
    itemName: [item?.itemName ?? '', [...trimRequired(2, 200)]],
    quantity: [item?.quantity ?? 1, [Validators.required, Validators.min(1)]],
    unitPrice: [item?.unitPrice ?? null, [Validators.required, Validators.min(0.01)]],
  });
}

export function createBillingForm(fb: FormBuilder): FormGroup {
  return fb.group({
    patientId: ['', Validators.required],
    appointment: [null],
    paymentMethod: ['CASH'],
    taxAmount: [0, Validators.min(0)],
    discountAmount: [0, Validators.min(0)],
    notes: ['', Validators.maxLength(1000)],
    dueDate: [null, [futureOrTodayDateValidator()]],
    insuranceProvider: [''],
    insuranceClaimNumber: [''],
    insuranceAmount: [0, Validators.min(0)],
    insuranceStatus: ['PENDING'],
    items: fb.array([createBillingItemGroup(fb)]),
  });
}

export function getBillingItems(form: FormGroup): FormArray {
  return form.get('items') as FormArray;
}





