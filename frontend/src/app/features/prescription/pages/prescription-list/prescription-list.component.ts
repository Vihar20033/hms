import { AuthService } from '../../../auth/services/auth.service';
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { Prescription } from '../../models/prescription.models';
import { PrescriptionService } from '../../services/prescription.service';
import { RouterLink } from '@angular/router';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { TableModule } from 'primeng/table';
import { canManagePrescriptions } from '../../utils/prescription-list.utils';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule],
  templateUrl: './prescription-list.component.html',
  styleUrl: './prescription-list.component.scss',
})
export class PrescriptionListComponent implements OnInit, OnDestroy {
  prescriptions: Prescription[] = [];
  isLoading = true;
  openMenuId: number | null = null;

  // Pagination
  currentPage = 0;
  pageSize = 15;
  isLastPage = false;
  isMoreLoading = false;
  searchQuery = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private prescriptionService: PrescriptionService,
    private authService: AuthService,
    private statusModalService: StatusModalService,
  ) { }

  ngOnInit(): void {
    this.searchSubject.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
    ).subscribe((query) => {
      this.searchQuery = query;
      this.loadPrescriptions();
    });
    this.loadPrescriptions();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPrescriptions(isLoadMore = false): void {
    if (isLoadMore) {
      this.isMoreLoading = true;
      this.currentPage++;
    } else {
      this.isLoading = true;
      this.currentPage = 0;
      this.prescriptions = [];
    }

    this.prescriptionService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          this.prescriptions = [...this.prescriptions, ...res.data.content];
          this.isLastPage = res.data.last;
        }
        this.isLoading = false;
        this.isMoreLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.isMoreLoading = false;
      },
    });
  }

  onSearch(event: Event): void {
    this.searchSubject.next((event.target as HTMLInputElement).value);
  }

  toggleMenu(id: number): void {
    this.openMenuId = this.openMenuId === id ? null : id;
  }

  onPrint(id: number): void {
    this.openMenuId = null;
    window.open(`/prescriptions/${id}`, '_blank');
  }

  onViewCloudReport(reportUrl: string): void {
    this.openMenuId = null;
    if (reportUrl) {
      window.open(reportUrl, '_blank');
    }
  }

  async onDelete(id: number): Promise<void> {
    this.openMenuId = null;
    const confirmed = await this.statusModalService.confirm(
      'Delete Prescription',
      'Delete this clinical record? This action cannot be undone.',
      'Delete',
    );
    if (!confirmed) return;

    this.prescriptionService.delete(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Prescription Deleted', 'The clinical record was removed.');
        this.loadPrescriptions();
      },
      error: (err) =>
        this.statusModalService.showError('Delete Failed', err.error?.message || 'Could not delete this prescription.'),
    });
  }

  get canManage(): boolean {
    return canManagePrescriptions(this.authService.getUserRole());
  }
}












