import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { Appointment } from '../../../../core/models/appointment.models';
import { Vitals } from '../../../../core/models/clinical.models';
import { ApiResponse, PagedResponse } from '../../../../core/models/common.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { VitalsService } from '../../../../core/services/vitals.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

interface AppointmentWithVitals extends Appointment {
  vitals?: Vitals;
}

@Component({
  selector: 'app-vitals-list',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    DialogModule,
    ReactiveFormsModule,
    SidebarComponent,
    HeaderComponent,
  ],
  templateUrl: './vitals-list.component.html',
  styleUrl: './vitals-list.component.scss',
})
export class VitalsListComponent implements OnInit {
  appointments: AppointmentWithVitals[] = [];
  isLoading = true;

  // Vitals Management
  vitalsDialogVisible = false;
  vitalsForm: FormGroup;
  isSavingVitals = false;
  editingVitalsId: number | null = null;
  currentAppointmentId: number | null = null;
  isReadOnly = false;

  constructor(
    private appointmentService: AppointmentService,
    private vitalsService: VitalsService,
    private authService: AuthService,
    private fb: FormBuilder,
    private router: Router,
  ) {
    this.vitalsForm = this.fb.group({
      temperature: [null, [Validators.min(30), Validators.max(45)]],
      systolicBP: [null, [Validators.min(60), Validators.max(250)]],
      diastolicBP: [null, [Validators.min(40), Validators.max(150)]],
      pulseRate: [null, [Validators.min(30), Validators.max(250)]],
      respiratoryRate: [null, [Validators.min(8), Validators.max(60)]],
      spo2: [null, [Validators.min(50), Validators.max(100)]],
      weight: [null, [Validators.min(0), Validators.max(300)]],
      height: [null, [Validators.min(0), Validators.max(250)]],
      notes: ['', Validators.maxLength(500)],
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;
    // Step 1: Fetch appointments (limited to 100 recent for brevity in vitals)
    this.appointmentService.search({ size: 100 }).subscribe({
      next: (res: ApiResponse<PagedResponse<Appointment>>) => {
        const filteredAppointments = (res.data.content || []).filter((a) => a.status !== 'CANCELLED');

        // Step 2: Fetch all vitals for today to match
        this.vitalsService.getAllToday().subscribe({
          next: (vitalsRes: ApiResponse<Vitals[]>) => {
            this.appointments = filteredAppointments
              .map((apt) => ({
                ...apt,
                vitals: vitalsRes.data.find((v) => v.appointmentId === apt.id),
              }))
              .sort((a, b) => {
                const dateA = new Date(a.appointmentTime || '').getTime();
                const dateB = new Date(b.appointmentTime || '').getTime();
                return dateB - dateA;
              });
            this.isLoading = false;
          },
          error: () => {
            this.appointments = filteredAppointments;
            this.isLoading = false;
          },
        });
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  canManageVitals(): boolean {
    const role = this.authService.getUserRole();
    return role === 'ADMIN' || role === 'NURSE';
  }

  openCollectVitals(appointmentId: number): void {
    this.currentAppointmentId = appointmentId;
    this.editingVitalsId = null;
    this.isReadOnly = false;
    this.vitalsForm.reset();
    this.vitalsForm.enable();
    this.vitalsDialogVisible = true;
  }

  viewVitals(vitals: Vitals): void {
    this.currentAppointmentId = vitals.appointmentId;
    this.editingVitalsId = vitals.id;
    this.isReadOnly = true;
    this.vitalsForm.patchValue(vitals);
    this.vitalsForm.disable();
    this.vitalsDialogVisible = true;
  }

  editFromView(): void {
    this.isReadOnly = false;
    this.vitalsForm.enable();
  }

  saveVitals(): void {
    if (this.vitalsForm.invalid || !this.currentAppointmentId) return;

    this.isSavingVitals = true;
    const data = this.vitalsForm.value;
    const requestData = { ...data, appointmentId: this.currentAppointmentId };

    const request = this.editingVitalsId
      ? this.vitalsService.updateVitals(this.editingVitalsId!, requestData)
      : this.vitalsService.recordVitals(requestData);

    request.subscribe({
      next: () => {
        this.vitalsDialogVisible = false;
        this.isSavingVitals = false;
        this.loadData();
      },
      error: () => {
        this.isSavingVitals = false;
      },
    });
  }

  deleteVitals(id: number): void {
    if (!confirm('Are you sure you want to delete this vital record?')) return;

    this.vitalsService.delete(id).subscribe({
      next: () => {
        this.loadData();
      },
    });
  }

  getStatusClass(status: string): string {
    return status.toLowerCase();
  }
}
