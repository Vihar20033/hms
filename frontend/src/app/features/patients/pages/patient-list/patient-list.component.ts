import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { ApiResponse } from '../../../../core/models/common.models';
import { BloodGroup, Patient, PatientSlice, UrgencyLevel } from '../../../../core/models/patient.models';
import { AuthService } from '../../../../core/services/auth.service';
import { ExcelExportService } from '../../../../core/services/excel-export.service';
import { PatientService } from '../../../../core/services/patient.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

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
export class PatientListComponent implements OnInit, OnDestroy {
  patients: Patient[] = [];
  isLoading = true;
  errorMessage = '';

  // Search Filters
  searchTerm = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();
  selectedBloodGroup = '';
  selectedUrgency = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  isLastPage = false;

  bloodGroups = Object.values(BloodGroup);
  urgencyLevels = Object.values(UrgencyLevel);

  constructor(
    private patientService: PatientService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private excelExportService: ExcelExportService,
  ) {}

  canRegister(): boolean {
    const role = this.authService.getUserRole();
    return role === 'ADMIN' || role === 'RECEPTIONIST' || role === 'NURSE';
  }

  canEdit(): boolean {
    const role = this.authService.getUserRole();
    return role === 'ADMIN' || role === 'DOCTOR' || role === 'RECEPTIONIST' || role === 'NURSE';
  }

  canDelete(): boolean {
    const role = this.authService.getUserRole();
    return role === 'ADMIN' || role === 'RECEPTIONIST';
  }

  ngOnInit(): void {
    this.loadPatients();

    this.searchSubject.pipe(debounceTime(400), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(() => {
      this.onSearch();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
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

  onSearch(): void {
    this.currentPage = 0;
    this.loadPatients();
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchTerm);
  }

  onPageChange(event: any): void {
    this.currentPage = event.first / event.rows;
    this.pageSize = event.rows;
    this.loadPatients();
  }

  getUrgencyClass(level: string): string {
    return `urgency-${level.toLowerCase()}`;
  }

  bloodGroupOptions(): Array<{ label: string; value: string }> {
    return this.bloodGroups.map((group) => ({
      label: group.replace('_POSITIVE', '+').replace('_NEGATIVE', '-'),
      value: group,
    }));
  }

  urgencyOptions(): Array<{ label: string; value: string }> {
    return this.urgencyLevels.map((level) => ({
      label: level,
      value: level,
    }));
  }

  editPatient(patientId: string): void {
    this.router.navigate(['/patients/register'], { queryParams: { patientId } });
  }

  deletePatient(patientId: string): void {
    if (!confirm('Are you sure you want to delete this patient record?')) return;

    this.patientService.delete(patientId).subscribe({
      next: () => {
        this.loadPatients();
      },
      error: () => {},
    });
  }

  exportToExcel(): void {
    const exportData = this.patients.map((p) => ({
      'Patient Name': p.name,
      Email: p.email,
      Age: p.age,
      'Contact Number': p.contactNumber,
      'Blood Group': p.bloodGroup.replace('_POSITIVE', '+').replace('_NEGATIVE', '-'),
      'Urgency Level': p.urgencyLevel,
      'Initial Diagnosis': p.prescription,
      'Consultation Fees': p.fees,
      'Registration Date': new Date(p.createdAt).toLocaleDateString(),
    }));

    this.excelExportService.exportAsExcelFile(exportData, 'Patient_Directory');
  }
}
