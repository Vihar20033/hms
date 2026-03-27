import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputSwitchModule } from 'primeng/inputswitch';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { BOOKABLE_DEPARTMENTS, formatDepartmentLabel } from '../../../../core/constants/department.constants';
import { Appointment } from '../../../../core/models/appointment.models';
import { ApiResponse } from '../../../../core/models/common.models';
import { Doctor } from '../../../../core/models/doctor.models';
import { Patient } from '../../../../core/models/patient.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { DoctorService } from '../../../../core/services/doctor.service';
import { PatientService } from '../../../../core/services/patient.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { createAppointmentBookingForm, updateAppointmentTimeValidators } from './appointment-booking-form';
import {
  buildDepartmentOptions,
  buildDoctorOptions,
  buildPatientOptions,
  filterAvailableDoctors,
  formatDateOnly,
  formatTimeOnly,
  toDateOnly,
  toTimeOnly,
} from './appointment-booking.utils';

@Component({
  selector: 'app-appointment-booking',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SidebarComponent,
    HeaderComponent,
    RouterLink,
    DropdownModule,
    CalendarModule,
    InputTextModule,
    InputTextareaModule,
    InputSwitchModule,
  ],
  templateUrl: './appointment-booking.component.html',
  styleUrl: './appointment-booking.component.scss',
})
export class AppointmentBookingComponent implements OnInit {
  bookingForm: FormGroup;
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  isLoading = false;
  errorMessage = '';
  isEditMode = false;
  appointmentId: number | null = null;
  currentUser$ = this.authService.currentUser$;

  departments = BOOKABLE_DEPARTMENTS;
  selectedDepartment: string = '';
  filteredDoctors: Doctor[] = [];
  formSubmitted = false; // Prevents past dates from being selected
  readonly minDate = new Date();

  constructor(
    private fb: FormBuilder,
    private appointmentService: AppointmentService,
    private patientService: PatientService,
    private doctorService: DoctorService,
    private router: Router,
    private route: ActivatedRoute,
    public authService: AuthService,
  ) {
    this.bookingForm = createAppointmentBookingForm(this.fb);
    this.bookingForm.get('isEmergency')?.valueChanges.subscribe((isEmergency) => {
      updateAppointmentTimeValidators(this.bookingForm, isEmergency);
    });
  }

  ngOnInit(): void {
    this.loadData();
    this.route.queryParams.subscribe((params) => {
      if (params['patientId']) {
        this.bookingForm.patchValue({ patientId: Number(params['patientId']) });
      }
      if (params['appointmentId']) {
        this.isEditMode = true;
        this.appointmentId = Number(params['appointmentId']);
        this.loadAppointmentForEdit(Number(params['appointmentId']));
      }
    });
  }

  loadData(): void {
    this.patientService.getAll().subscribe((res: ApiResponse<Patient[]>) => (this.patients = res.data));
    this.doctorService.getAll().subscribe((res: ApiResponse<Doctor[]>) => {
      this.doctors = res.data;
      this.filterDoctors();
    });
  }

  loadAppointmentForEdit(id: number): void {
    this.isLoading = true;
    this.appointmentService.getById(id).subscribe({
      next: (res: ApiResponse<Appointment>) => {
        const appointment = res.data;
        this.selectedDepartment = appointment.department;
        this.filterDoctors();
        this.bookingForm.patchValue({
          patientId: appointment.patientId,
          department: appointment.department,
          doctorId: appointment.doctorId ?? '',
          appointmentDate: toDateOnly(appointment.appointmentTime),
          appointmentTime: toTimeOnly(appointment.appointmentTime),
          reason: appointment.reason,
          notes: appointment.notes ?? '',
          isEmergency: appointment.isEmergency ?? false,
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load appointment details.';
        this.isLoading = false;
      },
    });
  }

  filterDoctors(): void {
    this.filteredDoctors = filterAvailableDoctors(this.doctors, this.selectedDepartment);

    if (this.filteredDoctors.length === 1) {
      this.bookingForm.patchValue({ doctorId: this.filteredDoctors[0].id });
    }
  }

  onDepartmentChange(dept: string | null): void {
    const selectedDept = dept ?? '';
    this.selectedDepartment = selectedDept;
    this.errorMessage = '';
    this.bookingForm.patchValue({ department: selectedDept });
    this.bookingForm.patchValue({ doctorId: '' });
    this.filterDoctors();

    if (selectedDept && this.filteredDoctors.length === 0) {
      this.errorMessage = `No active doctors are available for ${this.getDepartmentLabel(selectedDept)}. Add a doctor in Staff or choose another department.`;
    }
  }

  getDepartmentLabel(department: string): string {
    return formatDepartmentLabel(department);
  }

  onSubmit(): void {
    this.formSubmitted = true;
    this.errorMessage = '';

    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      if (this.bookingForm.get('doctorId')?.invalid && this.selectedDepartment && this.filteredDoctors.length === 0) {
        this.errorMessage = 'Please add a doctor for this department before booking the appointment.';
      } else {
        this.errorMessage = 'Please fill all required appointment details before confirming.';
      }
      return;
    }

    this.isLoading = true;

    const payload = {
      ...this.bookingForm.value,
      appointmentDate: formatDateOnly(this.bookingForm.get('appointmentDate')?.value),
      appointmentTime: formatTimeOnly(this.bookingForm.get('appointmentTime')?.value),
    };

    const request$ =
      this.isEditMode && this.appointmentId
        ? this.appointmentService.update(this.appointmentId!, payload)
        : this.appointmentService.create(payload);

    request$.subscribe({
      next: () => {
        this.router.navigate(['/appointments']);
      },
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Booking failed. Please try again.';
        this.isLoading = false;
      },
    });
  }

  patientOptions(): Array<{ label: string; value: number }> {
    return buildPatientOptions(this.patients);
  }

  departmentOptions(): Array<{ label: string; value: string }> {
    return buildDepartmentOptions(this.departments);
  }

  doctorOptions(): Array<{ label: string; value: number }> {
    return buildDoctorOptions(this.filteredDoctors);
  }
}
