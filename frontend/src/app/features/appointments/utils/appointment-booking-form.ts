import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { futureOrTodayDateValidator , clinicHoursValidator , trimRequired } from 'src/app/core/validators/app-validators';


export function createAppointmentBookingForm(fb: FormBuilder): FormGroup {
  return fb.group({
    patientId: ['', Validators.required],
    department: ['', Validators.required],
    doctorId: ['', Validators.required],
    appointmentDate: [null, [Validators.required, futureOrTodayDateValidator()]],
    appointmentTime: [null, [Validators.required, clinicHoursValidator(8, 20)]],
    reason: ['', [...trimRequired(3, 500)]],
    notes: ['', Validators.maxLength(2000)],
    isEmergency: [false],
  });
}

export function updateAppointmentTimeValidators(form: FormGroup, isEmergency: boolean): void {
  const timeControl = form.get('appointmentTime');

  if (isEmergency) {
    timeControl?.setValidators([Validators.required]);
  } else {
    timeControl?.setValidators([Validators.required, clinicHoursValidator(8, 20)]);
  }
  timeControl?.updateValueAndValidity();
}
