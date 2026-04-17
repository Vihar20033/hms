import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../layout/header/header.component';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../shared/services/status-modal.service';
import { AdminService } from '../services/admin.service';

type RestoreTargetKey = 'patient' | 'doctor' | 'user' | 'billing';

@Component({
  selector: 'app-system-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, HeaderComponent],
  templateUrl: './system-admin.component.html',
  styleUrl: './system-admin.component.scss',
})
export class SystemAdminComponent {
  patientId = '';
  doctorId = '';
  userId = '';
  billingId = '';

  loadingTarget: RestoreTargetKey | null = null;

  constructor(
    private adminService: AdminService,
    private statusModalService: StatusModalService,
  ) {}

  async restorePatient(): Promise<void> {
    await this.restoreEntity('patient', this.patientId, (id) => this.adminService.restorePatient(id), 'Patient');
  }

  async restoreDoctor(): Promise<void> {
    await this.restoreEntity('doctor', this.doctorId, (id) => this.adminService.restoreDoctor(id), 'Doctor');
  }

  async restoreUser(): Promise<void> {
    await this.restoreEntity('user', this.userId, (id) => this.adminService.restoreUser(id), 'User');
  }

  async restoreBilling(): Promise<void> {
    await this.restoreEntity('billing', this.billingId, (id) => this.adminService.restoreBilling(id), 'Billing record');
  }

  private async restoreEntity(
    target: RestoreTargetKey,
    rawId: string,
    requestFactory: (id: number) => ReturnType<AdminService['restorePatient']>,
    label: string,
  ): Promise<void> {
    const id = Number(rawId);
    if (!Number.isInteger(id) || id <= 0) {
      this.statusModalService.showWarning('Invalid ID', `Enter a valid ${label.toLowerCase()} ID.`);
      return;
    }

    const confirmed = await this.statusModalService.confirm(
      `Restore ${label}`,
      `Restore ${label.toLowerCase()} #${id}? This will mark the soft-deleted record as active again.`,
      'Restore',
      'Cancel',
    );

    if (!confirmed) {
      return;
    }

    this.loadingTarget = target;
    requestFactory(id).subscribe({
      next: (res) => {
        this.loadingTarget = null;
        this.statusModalService.showSuccess(`${label} Restored`, res.message || `${label} restored successfully.`);
      },
      error: (err: HttpErrorResponse) => {
        this.loadingTarget = null;
        this.statusModalService.showError(
          `${label} Restore Failed`,
          err.error?.message || `Could not restore ${label.toLowerCase()}.`,
        );
      },
    });
  }

  isLoading(target: RestoreTargetKey): boolean {
    return this.loadingTarget === target;
  }
}
