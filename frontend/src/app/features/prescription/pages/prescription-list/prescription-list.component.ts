import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Prescription } from '../../models/prescription.models';
import { PrescriptionService } from '../../services/prescription.service';
import { canManagePrescriptions } from '../../utils/prescription-list.utils';

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
  pageSize = 20;
  isFirstPage = true;
  hasNextPage = false;
  searchQuery = '';
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  constructor(
    private prescriptionService: PrescriptionService,
    private authService: AuthService,
    private statusModalService: StatusModalService,
  ) {}

  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(350), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe((query) => {
      this.searchQuery = query;
      this.loadPrescriptions(0);
    });
    this.loadPrescriptions(0);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadPrescriptions(page = 0): void {
    this.isLoading = true;
    this.currentPage = Math.max(page, 0);

    this.prescriptionService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          this.prescriptions = res.data.content;
          this.isFirstPage = res.data.first;
          this.hasNextPage = res.data.hasNext;
        }
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  previousPage(): void {
    if (this.isLoading || this.isFirstPage) return;
    this.loadPrescriptions(this.currentPage - 1);
  }

  nextPage(): void {
    if (this.isLoading || !this.hasNextPage) return;
    this.loadPrescriptions(this.currentPage + 1);
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
