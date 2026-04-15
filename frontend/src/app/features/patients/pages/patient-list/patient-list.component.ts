import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DialogService, DynamicDialogModule } from 'primeng/dynamicdialog';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { HistoryDialogComponent } from '../../../../shared/components/history-dialog/history-dialog.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Patient } from '../../models/patient.models';
import { PatientService } from '../../services/patient.service';
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
    InputTextModule,
    DynamicDialogModule,
  ],
  providers: [DialogService],
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.scss',
})
export class PatientListComponent implements OnInit {
  patients: Patient[] = [];
  isLoading = true;
  errorMessage = '';
  openMenuId: number | null = null;

  // Search & Pagination
  searchQuery = '';
  searchSubject = new Subject<string>();
  currentPage = 0;
  pageSize = 20;
  isLastPage = false;
  isMoreLoading = false;

  constructor(
    private patientService: PatientService,
    private authService: AuthService,
    private router: Router,
    private statusModalService: StatusModalService,
    private dialogService: DialogService,
  ) {
    this.searchSubject.pipe(debounceTime(400), distinctUntilChanged()).subscribe((query) => {
      this.searchQuery = query;
      this.loadPatients();
    });
  }

  ngOnInit(): void {
    this.loadPatients();
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchSubject.next(query);
  }

  loadPatients(isLoadMore = false): void {
    if (isLoadMore) {
      this.isMoreLoading = true;
      this.currentPage++;
    } else {
      this.isLoading = true;
      this.currentPage = 0;
      this.patients = [];
    }

    this.patientService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          const newPatients = res.data.content;
          this.patients = isLoadMore ? [...this.patients, ...newPatients] : newPatients;
          this.isLastPage = res.data.last;
        }
        this.isLoading = false;
        this.isMoreLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load patients.';
        this.isLoading = false;
        this.isMoreLoading = false;
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
    this.openMenuId = null;
    this.router.navigate(['/patients/register'], { queryParams: { patientId } });
  }

  async deletePatient(patientId: number): Promise<void> {
    this.openMenuId = null;
    const confirmed = await this.statusModalService.confirm('Delete Patient', 'Delete this patient record?', 'Delete');
    if (!confirmed) return;

    this.patientService.delete(patientId).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Patient Deleted', 'The patient record was removed.');
        this.loadPatients();
      },
      error: (err) =>
        this.statusModalService.showError('Delete Failed', err.error?.message || 'Could not delete this patient.'),
    });
  }

  viewHistory(patient: Patient): void {
    this.openMenuId = null;
    this.dialogService.open(HistoryDialogComponent, {
      data: {
        entityType: 'patient',
        id: patient.id,
        title: patient.name,
      },
      header: 'Audit History',
      width: '70%',
      contentStyle: { 'max-height': '600px', overflow: 'auto' },
      baseZIndex: 10000,
    });
  }

  toggleMenu(id: number): void {
    this.openMenuId = this.openMenuId === id ? null : id;
  }
}
