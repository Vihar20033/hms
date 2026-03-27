import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { ApiResponse } from '../../../../core/models/common.models';
import { Prescription } from '../../../../core/models/prescription.models';
import { AuthService } from '../../../../core/services/auth.service';
import { PrescriptionService } from '../../../../core/services/prescription.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
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

  constructor(
    private prescriptionService: PrescriptionService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.loadPrescriptions();
  }

  loadPrescriptions(): void {
    this.isLoading = true;
    this.prescriptionService.getAll().subscribe({
      next: (res: ApiResponse<Prescription[]>) => {
        this.prescriptions = res.data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading prescriptions:', error);
        this.isLoading = false;
      },
    });
  }

  onPrint(id: number): void {
    window.open(`/prescriptions/${id}`, '_blank');
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
