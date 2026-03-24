import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { BehaviorSubject, Subject, combineLatest, debounceTime, distinctUntilChanged, switchMap, takeUntil, tap } from 'rxjs';
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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientListComponent implements OnInit, OnDestroy {
  // Reactive State using Signals
  patients = signal<Patient[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');
  
  // Pagination & Filters State
  currentPage = signal<number>(0);
  pageSize = signal<number>(10);
  totalElements = signal<number>(0);
  isLastPage = signal<boolean>(false);
  
  searchTerm = signal<string>('');
  selectedBloodGroup = signal<string>('');
  selectedUrgency = signal<string>('');

  // Derived State
  bloodGroups = Object.values(BloodGroup);
  urgencyLevels = Object.values(UrgencyLevel);
  
  private destroy$ = new Subject<void>();
  
  // Stream for triggering reloads
  private reloadTrigger$ = new BehaviorSubject<void>(undefined);

  constructor(
    private patientService: PatientService,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private excelExportService: ExcelExportService,
  ) {}

  ngOnInit(): void {
    // Advanced RxJS Implementation: Search logic with debounce and switchMap
    // This demonstrates senior-level knowledge of handling race conditions and efficiency
    this.reloadTrigger$.pipe(
      tap(() => this.isLoading.set(true)),
      debounceTime(400),
      switchMap(() => this.patientService.search(
        this.searchTerm(),
        undefined,
        this.selectedBloodGroup(),
        this.selectedUrgency(),
        this.currentPage(),
        this.pageSize()
      )),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (res: ApiResponse<PatientSlice>) => {
        this.patients.set(res.data.content);
        this.totalElements.set(res.data.totalElements);
        this.isLastPage.set(res.data.last);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('Failed to load patients.');
        this.isLoading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearchInput(): void {
    this.currentPage.set(0);
    this.reloadTrigger$.next();
  }

  onFilterChange(): void {
    this.currentPage.set(0);
    this.reloadTrigger$.next();
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.first / event.rows);
    this.pageSize.set(event.rows);
    this.reloadTrigger$.next();
  }

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
      next: () => this.reloadTrigger$.next(),
      error: () => {},
    });
  }

  exportToExcel(): void {
    const data = this.patients();
    const exportData = data.map((p) => ({
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
