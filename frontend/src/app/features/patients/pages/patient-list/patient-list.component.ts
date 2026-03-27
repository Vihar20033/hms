import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ApiResponse } from '../../../../core/models/common.models';
import { BloodGroup, Patient, PatientSlice, UrgencyLevel } from '../../../../core/models/patient.models';
import { AuthService } from '../../../../core/services/auth.service';
import { PatientService } from '../../../../core/services/patient.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { buildBloodGroupOptions, buildUrgencyOptions, getUrgencyClass } from '../../utils/patient-form.utils';
import { canDeletePatient, canEditPatient, canRegisterPatient } from '../../utils/patient-list.utils';

@Component({
  selector: 'app-patient-list',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    FormsModule,
    RouterLink,
    InputTextModule,
    DropdownModule,
    TableModule,
  ],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.scss',
})
export class PatientListComponent implements OnInit {
  patients: Patient[] = [];
  isLoading = true;
  errorMessage = '';

  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  isLastPage = false;

  searchTerm = '';
  selectedBloodGroup = '';
  selectedUrgency = '';

  bloodGroups = Object.values(BloodGroup);
  urgencyLevels = Object.values(UrgencyLevel);

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
    this.patientService
      .search(
        this.searchTerm,
        undefined,
        this.selectedBloodGroup,
        this.selectedUrgency,
        this.currentPage,
        this.pageSize,
      )
      .subscribe({
        next: (res: ApiResponse<PatientSlice>) => {
          this.patients = res.data.content;
          this.totalElements = res.data.totalElements;
          this.isLastPage = res.data.last;
          this.isLoading = false;
        },
        error: () => {
          this.errorMessage = 'Failed to load patients.';
          this.isLoading = false;
        },
      });
  }

  onSearchInput(): void {
    this.currentPage = 0;
    this.loadPatients();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadPatients();
  }

  onPageChange(event: any): void {
    this.currentPage = event.first / event.rows;
    this.pageSize = event.rows;
    this.loadPatients();
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

  bloodGroupOptions(): Array<{ label: string; value: string }> {
    return buildBloodGroupOptions(this.bloodGroups);
  }

  urgencyOptions(): Array<{ label: string; value: string }> {
    return buildUrgencyOptions(this.urgencyLevels);
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
