import { FormArray, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { trimRequired } from '../../../core/validators/app-validators';

export function createPrescriptionMedicineGroup(fb: FormBuilder): FormGroup {
  return fb.group(
    {
      medicineId: [''],
      availableStock: [0],
      medicineName: ['', [...trimRequired(2, 200)]],
      dosage: ['', [...trimRequired(1, 200)]],
      duration: ['', [...trimRequired(1, 100)]],
      quantity: [1, [Validators.required, Validators.min(1)]],
      instructions: ['', Validators.maxLength(500)],
    },
    { validators: stockValidator },
  );
}

export function createPrescriptionForm(fb: FormBuilder): FormGroup {
  return fb.group({
    symptoms: ['', Validators.maxLength(1000)],
    diagnosis: ['', [...trimRequired(2, 1000)]],
    medicines: fb.array([createPrescriptionMedicineGroup(fb)]),
    advice: ['', Validators.maxLength(1000)],
    notes: ['', Validators.maxLength(2000)],
  });
}

export function getPrescriptionMedicines(form: FormGroup): FormArray {
  return form.get('medicines') as FormArray;
}

export function stockValidator(group: FormGroup): ValidationErrors | null {
  const qty = group.get('quantity')?.value;
  const stock = group.get('availableStock')?.value;
  const medName = group.get('medicineName')?.value;

  if (medName && qty > stock && stock !== null) {
    return { insufficientStock: { actual: stock, requested: qty } };
  }

  return null;
}

export function calculatePrescriptionQuantity(group: FormGroup): void {
  const dosage = group.get('dosage')?.value || '';
  const duration = group.get('duration')?.value || '';

  if (!dosage || !duration) {
    return;
  }

  const dosesArray = dosage.match(/\d+/g);
  const dosesPerDay = dosesArray ? dosesArray.reduce((acc: number, val: string) => acc + parseInt(val, 10), 0) : 0;

  const durationMatch = duration.match(/\d+/);
  let durationInDays = durationMatch ? parseInt(durationMatch[0], 10) : 0;

  if (duration.toLowerCase().includes('week')) {
    durationInDays *= 7;
  } else if (duration.toLowerCase().includes('month')) {
    durationInDays *= 30;
  }

  if (dosesPerDay > 0 && durationInDays > 0) {
    group.patchValue(
      {
        quantity: dosesPerDay * durationInDays,
      },
      { emitEvent: false },
    );
  }
}
