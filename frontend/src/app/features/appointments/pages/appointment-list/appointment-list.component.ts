import { ApiResponse } from '../../../../core/models/common.models';
import { Appointment, AppointmentStatus, AppointmentSummary } from '../../models/appointment.models';
import { AppointmentService } from '../../services/appointment.service';
import { AuthService } from '../../../auth/services/auth.service';
import { Billing } from '../../../billing/models/billing.models';
import { BillingService } from '../../../billing/services/billing.service';
import { canDoctorStartAppointment, canManageAppointmentForRole, getAppointmentPageLead, getAppointmentStatusClass, getAppointmentWorkflowLabel } from '../../utils/appointment-list.utils';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DialogModule } from 'primeng/dialog';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { Prescription } from '../../../prescription/models/prescription.models';
import { Router, RouterLink } from '@angular/router';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { TableModule } from 'primeng/table';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule, DialogModule],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentListComponent implements OnInit {
  appointments: Appointment[] = [];
  summary: AppointmentSummary | null = null;
  isLoading = true;
  
  // Detail Modal State
  detailModalVisible = false;
  selectedAppointment: Appointment | null = null;

  selectedStatusFilter: 'ALL' | AppointmentStatus = 'ALL';

  statusEnum = AppointmentStatus;
  userRole: string | null = null;
  pageLead = '';

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private router: Router,
    private billingService: BillingService,
    private cdr: ChangeDetectorRef,
    private statusModalService: StatusModalService,
  ) {}

  ngOnInit(): void {
    
    this.userRole = this.authService.getUserRole();

    if (this.userRole === 'RECEPTIONIST') {
      this.selectedStatusFilter = AppointmentStatus.SCHEDULED;
    }

    this.pageLead = getAppointmentPageLead(this.userRole);
    this.loadSummary();
    this.loadAppointments();
  }

  loadSummary(): void {
    this.appointmentService.getSummary().subscribe({
      next: (res: ApiResponse<AppointmentSummary>) => {
        this.summary = res.data;
        this.cdr.markForCheck();
      },
    });
  }

  loadAppointments(): void {
    this.isLoading = true;

    const params = {
      status: this.selectedStatusFilter === 'ALL' ? undefined : (this.selectedStatusFilter as AppointmentStatus),
    };

    this.appointmentService.getAll(params).subscribe({
      next: (res: ApiResponse<Appointment[]>) => {
        this.appointments = res.data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  onRowSelect(appointment: Appointment): void {
    // Optimization: Pre-fill with current data while fetching fresh state
    this.selectedAppointment = {...appointment};
    this.detailModalVisible = true;
    this.cdr.markForCheck();

    // Fetch fresh data to ensure hasPrescription is accurate after prescription flow
    this.appointmentService.getById(appointment.id).subscribe({
      next: (res: ApiResponse<Appointment>) => {
        this.selectedAppointment = res.data;
        // Also update in the list to maintain consistency
        const idx = this.appointments.findIndex(a => a.id === appointment.id);
        if (idx !== -1) {
          this.appointments[idx] = res.data;
        }
        this.cdr.markForCheck();
      }
    });
  }

  onCheckIn(id: number): void {
    this.appointmentService.checkIn(id).subscribe(() => {
      this.loadSummary();
      this.loadAppointments();
    });
  }

  onStart(id: number): void {
    this.appointmentService.startConsultation(id).subscribe(() => {
      this.loadSummary();
      this.loadAppointments();
    });
  }

  onComplete(id: number): void {
    this.appointmentService.completeConsultation(id).subscribe(() => {
      this.loadSummary();
      this.loadAppointments();
    });
  }

  onEdit(id: number): void {
    this.router.navigate(['/appointments/book'], { queryParams: { appointmentId: id } });
  }

  async onCancel(id: number): Promise<void> {
    const confirmed = await this.statusModalService.confirm('Cancel Appointment', 'Cancel this appointment?', 'Cancel Appointment');
    if (!confirmed) return;

    this.appointmentService.updateStatus(id, AppointmentStatus.CANCELLED).subscribe(() => {
      this.statusModalService.showSuccess('Appointment Cancelled', 'The appointment status was updated.');
      this.loadSummary();
      this.loadAppointments();
    });
  }

  async onDelete(id: number): Promise<void> {
    const confirmed = await this.statusModalService.confirm('Delete Appointment', 'Delete this appointment?', 'Delete');
    if (!confirmed) return;

    this.appointmentService.delete(id).subscribe(() => {
      this.statusModalService.showSuccess('Appointment Deleted', 'The appointment was removed.');
      this.loadSummary();
      this.loadAppointments();
    });
  }

  onGenerateBill(appointmentId: number): void {
    this.isLoading = true;
    this.billingService.generateFromAppointment(appointmentId).subscribe({
      next: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
        this.router.navigate(['/billing']);
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  canManageAppointment(): boolean {
    return canManageAppointmentForRole(this.userRole);
  }

  setStatusFilter(filter: 'ALL' | AppointmentStatus): void {
    this.selectedStatusFilter = filter;
    this.loadAppointments();
  }

  getStatusClass(status: string): string {
    return getAppointmentStatusClass(status);
  }

  getWorkflowLabel(appointment: Appointment): string {
    return getAppointmentWorkflowLabel(appointment, this.userRole);
  }

  canDoctorStart(appointment: Appointment): boolean {
    return canDoctorStartAppointment(appointment, this.userRole);
  }
}












