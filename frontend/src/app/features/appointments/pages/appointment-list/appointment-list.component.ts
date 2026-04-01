import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Appointment, AppointmentStatus, AppointmentSummary } from '../../../../core/models/appointment.models';
import { ApiResponse } from '../../../../core/models/common.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import {
  canDoctorStartAppointment,
  canManageAppointmentForRole,
  getAppointmentPageLead,
  getAppointmentStatusClass,
  getAppointmentWorkflowLabel,
} from '../../utils/appointment-list.utils';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppointmentListComponent implements OnInit {
  appointments: Appointment[] = [];
  summary: AppointmentSummary | null = null;
  isLoading = true;

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

  onDelete(id: number): void {
    if (!confirm('Delete this appointment?')) return;
    this.appointmentService.delete(id).subscribe(() => {
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
