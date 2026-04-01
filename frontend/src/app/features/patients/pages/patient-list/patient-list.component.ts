import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ApiResponse } from '../../../../core/models/common.models';
import { Patient } from '../../../../core/models/patient.models';
import { AuthService } from '../../../../core/services/auth.service';
import { PatientService } from '../../../../core/services/patient.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { getUrgencyClass } from '../../utils/patient-form.utils';
import { canDeletePatient, canEditPatient, canRegisterPatient } from '../../utils/patient-list.utils';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    RouterLink,
    TableModule,
  ],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.scss',
})
export class PatientListComponent implements OnInit {
  patients: Patient[] = [];
  isLoading = true;
  errorMessage = '';

  constructor(
    private patientService: PatientService,
    private authService: AuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.isLoading = true;
    this.patientService.getAll().subscribe({
      next: (res: ApiResponse<Patient[]>) => {
        this.patients = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load patients.';
        this.isLoading = false;
      },
    });
  }

  canRegister(): boolean {
    return canRegisterPatient(this.authService.getUserRole());
  }

  canEdit(): boolean {
    return canEditPatient(this.authService.getUserRole());
  }

  canDelete(): boolean {
    return canDeletePatient(this.authService.getUserRole());
  }

  getUrgencyClass(level: string): string {
    return getUrgencyClass(level);
  }

  editPatient(patientId: number): void {
    this.router.navigate(['/patients/register'], { queryParams: { patientId } });
  }

  deletePatient(patientId: number): void {
    if (!confirm('Are you sure you want to delete this patient record?')) return;

    this.patientService.delete(patientId).subscribe({
      next: () => this.loadPatients(),
      error: () => {},
    });
  }
}
