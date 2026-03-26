import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import { Appointment } from '../../../../core/models/appointment.models';
import { ApiResponse, PagedResponse } from '../../../../core/models/common.models';
import { Patient } from '../../../../core/models/patient.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { PatientService } from '../../../../core/services/patient.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-patient-detail',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    RouterLink,
    DialogModule,
    TableModule,
    ReactiveFormsModule,
    DropdownModule,
  ],
  templateUrl: './patient-detail.component.html',
  styleUrl: './patient-detail.component.scss',
})
export class PatientDetailComponent implements OnInit {
  patient: Patient | null = null;
  appointments: Appointment[] = [];
  isLoading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private patientService: PatientService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.loadPatient(id);
      this.loadAppointments(id);
    }
  }

  loadPatient(id: number): void {
    this.isLoading = true;
    this.patientService.getById(id).subscribe({
      next: (res: ApiResponse<Patient>) => {
        this.patient = res.data;
        this.isLoading = false;
      },
      error: (_err: HttpErrorResponse) => {
        this.error = 'Failed to load patient details.';
        this.isLoading = false;
      },
    });
  }



  loadAppointments(patientId: number): void {
    this.appointmentService.getByPatientId(patientId).subscribe({
      next: (res: ApiResponse<PagedResponse<Appointment>>) => {
        this.appointments = (res.data.content || [])
          .map((a: Appointment) => ({ ...a, label: new Date(a.appointmentTime).toLocaleString() }));
      },
    });
  }



  getUrgencyClass(level: string | undefined): string {
    return level ? `urgency-${level.toLowerCase()}` : '';
  }
}
