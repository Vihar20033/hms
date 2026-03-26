import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
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
import {
  clinicHoursValidator,
  futureOrTodayDateValidator,
  trimRequired,
} from '../../../../core/validators/app-validators';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

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
  formSubmitted = false;          // Prevents past dates from being selected
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

    // Initialize form with validators
    this.bookingForm = this.fb.group({
      patientId: ['', Validators.required],
      department: ['', Validators.required],
      doctorId: ['', Validators.required],
      appointmentDate: [null, [Validators.required, futureOrTodayDateValidator()]],
      appointmentTime: [null, [Validators.required, clinicHoursValidator(8, 20)]],
      reason: ['', [...trimRequired(3, 500)]],
      notes: ['', Validators.maxLength(2000)],
      isEmergency: [false],
    });

    // Listen for emergency toggle to override clinic hours
    this.bookingForm.get('isEmergency')?.valueChanges.subscribe((isEmergency) => {
      const timeControl = this.bookingForm.get('appointmentTime');
      if (isEmergency) {
        timeControl?.setValidators([Validators.required]);
      } else {
        timeControl?.setValidators([Validators.required, clinicHoursValidator(8, 20)]);
      }
      timeControl?.updateValueAndValidity();
    });
  }

  // Initialize component and load data
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
          appointmentDate: this.toDateOnly(appointment.appointmentTime),
          appointmentTime: this.toTimeOnly(appointment.appointmentTime),
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
    if (!this.selectedDepartment) {
      this.filteredDoctors = [];
    } else {
      this.filteredDoctors = this.doctors
        .filter((d) => d.department === this.selectedDepartment && d.isAvailable)
        .sort((left, right) =>
          `${left.firstName} ${left.lastName}`.localeCompare(`${right.firstName} ${right.lastName}`),
        );
    }

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
      appointmentDate: this.formatDate(this.bookingForm.get('appointmentDate')?.value),
      appointmentTime: this.formatTime(this.bookingForm.get('appointmentTime')?.value),
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
    return this.patients.map((patient) => ({
      label: `${patient.name} (ID: ${patient.id})`,
      value: patient.id,
    }));
  }

  departmentOptions(): Array<{ label: string; value: string }> {
    return this.departments.map((department) => ({
      label: this.getDepartmentLabel(department),
      value: department,
    }));
  }

  doctorOptions(): Array<{ label: string; value: number }> {
    return this.filteredDoctors.map((doctor) => ({
      label: `Dr. ${doctor.firstName} ${doctor.lastName} - ${doctor.specialization}`,
      value: doctor.id,
    }));
  }

  private formatDate(value: Date | null): string | null {
    if (!value) {
      return null;
    }

    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatTime(value: Date | null): string | null {
    if (!value) {
      return null;
    }

    const hours = String(value.getHours()).padStart(2, '0');
    const minutes = String(value.getMinutes()).padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  private toDateOnly(value: string | null | undefined): Date | null {
    if (!value) {
      return null;
    }

    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  private toTimeOnly(value: string | null | undefined): Date | null {
    if (!value) {
      return null;
    }

    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return null;
    }

    const time = new Date();
    time.setHours(parsed.getHours(), parsed.getMinutes(), 0, 0);
    return time;
  }
}
