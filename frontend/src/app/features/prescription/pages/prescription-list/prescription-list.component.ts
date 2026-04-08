import { AuthService } from '../../../auth/services/auth.service';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { Prescription } from '../../models/prescription.models';
import { PrescriptionService } from '../../services/prescription.service';
import { RouterLink } from '@angular/router';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { TableModule } from 'primeng/table';
import { canManagePrescriptions } from '../../utils/prescription-list.utils';

@Component({
  selector: 'app-prescription-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule],
  templateUrl: './prescription-list.component.html',
  styleUrl: './prescription-list.component.scss',
})
export class PrescriptionListComponent implements OnInit {
  prescriptions: Prescription[] = [];
  isLoading = true;

  // Pagination
  currentPage = 0;
  pageSize = 15;
  isLastPage = false;
  isMoreLoading = false;

  constructor(
    private prescriptionService: PrescriptionService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.loadPrescriptions();
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

    this.prescriptionService.getSlice(this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        if (res.data) {
          this.prescriptions = [...this.prescriptions, ...res.data.content];
          this.isLastPage = res.data.last;
        }
        this.isLoading = false;
        this.isMoreLoading = false;
      },
      error: (error) => {
        console.error('Error loading prescriptions:', error);
        this.isLoading = false;
        this.isMoreLoading = false;
      },
    });
  }

  onPrint(id: number): void {
    window.open(`/prescriptions/${id}`, '_blank');
  }

  onViewCloudReport(reportUrl: string): void {
    if (reportUrl) {
      window.open(reportUrl, '_blank');
    }
  }

  onDelete(id: number): void {
    if (confirm('Are you sure you want to delete this clinical record? This action cannot be undone.')) {
      this.prescriptionService.delete(id).subscribe({
        next: () => {
          this.loadPrescriptions();
        },
        error: () => {
          alert('Error deleting prescription');
        },
      });
    }
  }

  get canManage(): boolean {
    return canManagePrescriptions(this.authService.getUserRole());
  }
}












