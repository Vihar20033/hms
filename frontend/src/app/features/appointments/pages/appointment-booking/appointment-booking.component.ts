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
} from '../../utils/appointment-booking.utils';

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
  filteredDoctors: Doctor[] = [];
  isLoading = false;
  errorMessage = '';
  isEditMode = false;
  appointmentId: number | null = null;
  formSubmitted = false;
  readonly minDate = new Date();
  readonly departments = BOOKABLE_DEPARTMENTS;

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
  }

  ngOnInit(): void {
    this.initializeFormListeners();
    this.loadInitialData();
    this.processQueryParams();
  }

  private initializeFormListeners(): void {
    // React to Emergency toggle
    this.bookingForm.get('isEmergency')?.valueChanges.subscribe((isEmergency) => {
      updateAppointmentTimeValidators(this.bookingForm, isEmergency);
    });

    // React to Department changes
    this.bookingForm.get('department')?.valueChanges.subscribe((dept) => {
      this.filterDoctors(dept);
    });
  }

  private loadInitialData(): void {
    this.patientService.getAll().subscribe((res) => (this.patients = res.data));
    this.doctorService.getAll().subscribe((res) => {
      this.doctors = res.data;
      this.filterDoctors(this.bookingForm.get('department')?.value);
    });
  }

  private processQueryParams(): void {
    this.route.queryParams.subscribe((params) => {
      if (params['patientId']) {
        this.bookingForm.patchValue({ patientId: Number(params['patientId']) });
      }
      if (params['appointmentId']) {
        this.isEditMode = true;
        this.appointmentId = Number(params['appointmentId']);
        this.loadAppointmentForEdit(this.appointmentId);
      }
    });
  }

  private loadAppointmentForEdit(id: number): void {
    this.isLoading = true;
    this.appointmentService.getById(id).subscribe({
      next: (res: ApiResponse<Appointment>) => {
        const appt = res.data;
        this.bookingForm.patchValue({
          patientId: appt.patientId,
          department: appt.department,
          doctorId: appt.doctorId ?? '',
          appointmentDate: toDateOnly(appt.appointmentTime),
          appointmentTime: toTimeOnly(appt.appointmentTime),
          reason: appt.reason,
          notes: appt.notes ?? '',
          isEmergency: appt.isEmergency ?? false,
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load appointment details.';
        this.isLoading = false;
      },
    });
  }

  filterDoctors(department: string): void {
    this.filteredDoctors = filterAvailableDoctors(this.doctors, department);
    this.errorMessage = '';

    // Auto-select if only one doctor
    if (this.filteredDoctors.length === 1) {
      this.bookingForm.get('doctorId')?.setValue(this.filteredDoctors[0].id);
    } else if (!this.isEditMode) {
      this.bookingForm.get('doctorId')?.setValue('');
    }

    // Show warning if no doctors available
    if (department && this.filteredDoctors.length === 0) {
      this.errorMessage = `No doctors available for ${formatDepartmentLabel(department)}.`;
    }
  }

  onSubmit(): void {
    this.formSubmitted = true;
    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      this.errorMessage = 'Please complete all required fields.';
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
        ? this.appointmentService.update(this.appointmentId, payload)
        : this.appointmentService.create(payload);

    request$.subscribe({
      next: () => this.router.navigate(['/appointments']),
      error: (err: HttpErrorResponse) => {
        this.errorMessage = err.error?.message || 'Booking failed. Please try again.';
        this.isLoading = false;
      },
    });
  }

  // Helpers for template
  get patientOptions() { return buildPatientOptions(this.patients); }
  get departmentOptions() { return buildDepartmentOptions(this.departments); }
  get doctorOptions() { return buildDoctorOptions(this.filteredDoctors); }
  getDepartmentLabel = (dept: string) => formatDepartmentLabel(dept);
}
