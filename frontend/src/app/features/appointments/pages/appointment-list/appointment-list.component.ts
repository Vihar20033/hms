import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Appointment, AppointmentStatus, AppointmentSummary } from '../../../../core/models/appointment.models';
import { ApiResponse, PagedResponse } from '../../../../core/models/common.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule, PaginatorModule],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})

export class AppointmentListComponent implements OnInit {
  
  appointments: Appointment[] = [];
  summary: AppointmentSummary | null = null;
  isLoading = true;
  
  selectedStatusFilter: 'ALL' | AppointmentStatus = 'ALL';
  page = 0;
  size = 10;
  totalElements = 0;
  
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
    
    this.setPageLead();
    this.loadSummary();
    this.loadAppointments();
  }

  setPageLead(): void {
    if (this.userRole === 'DOCTOR') {
      this.pageLead = 'Track only the patients assigned to you and start each consultation from here.';
    } else if (this.userRole === 'RECEPTIONIST') {
      this.pageLead = 'Manage arrivals, move patients into the doctor queue, and keep the OPD flowing.';
    } else {
      this.pageLead = 'View and manage all medical consultations.';
    }
  }

  loadSummary(): void {
    this.appointmentService.getSummary().subscribe({
      next: (res: ApiResponse<AppointmentSummary>) => {
        this.summary = res.data;
        this.cdr.markForCheck();
      }
    });
  }

  loadAppointments(): void {
    this.isLoading = true;
    
    const params = {
      page: this.page,
      size: this.size,
      status: this.selectedStatusFilter === 'ALL' ? undefined : this.selectedStatusFilter as AppointmentStatus,
    };

    this.appointmentService.search(params).subscribe({
      next: (res: ApiResponse<PagedResponse<Appointment>>) => {
        this.appointments = res.data.content;
        this.totalElements = res.data.totalElements;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  onPageChange(event: PaginatorState): void {
    this.page = event.first! / event.rows!;
    this.size = event.rows!;
    this.loadAppointments();
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
    return this.userRole === 'ADMIN' || this.userRole === 'RECEPTIONIST';
  }

  setStatusFilter(filter: 'ALL' | AppointmentStatus): void {
    this.selectedStatusFilter = filter;
    this.page = 0; 
    this.loadAppointments();
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getWorkflowLabel(appointment: Appointment): string {
    switch (appointment.status) {
      case AppointmentStatus.SCHEDULED:
        return this.userRole === 'DOCTOR' ? 'Waiting for reception check-in' : 'Scheduled and awaiting arrival';
      case AppointmentStatus.CHECKED_IN:
        return this.userRole === 'DOCTOR' ? 'Ready to start consultation' : 'Vitals and doctor handoff pending';
      case AppointmentStatus.IN_CONSULTATION:
        return 'Consultation in progress';
      case AppointmentStatus.COMPLETED:
        return 'Consultation complete';
      case AppointmentStatus.CANCELLED:
        return 'Visit cancelled';
      default:
        return 'Active appointment';
    }
  }

  canDoctorStart(appointment: Appointment): boolean {
    return (
      appointment.status === AppointmentStatus.CHECKED_IN && (this.userRole === 'DOCTOR' || this.userRole === 'ADMIN')
    );
  }
}
