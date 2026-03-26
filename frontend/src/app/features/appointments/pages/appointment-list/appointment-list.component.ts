import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, signal, computed } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Appointment, AppointmentStatus, AppointmentSummary } from '../../../../core/models/appointment.models';
import { Billing } from '../../../../core/models/billing.models';
import { ApiResponse, PagedResponse } from '../../../../core/models/common.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { ExcelExportService } from '../../../../core/services/excel-export.service';
import { PaginatorModule } from 'primeng/paginator';

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
export class AppointmentListComponent implements OnInit, OnDestroy {
  
  pagedResponse = signal<PagedResponse<Appointment> | null>(null);
  summary = signal<AppointmentSummary | null>(null);
  isLoading = signal<boolean>(true);
  
  selectedStatusFilter = signal<'ALL' | AppointmentStatus>('ALL');
  page = signal<number>(0);
  size = signal<number>(10);
  
  appointments = computed(() => this.pagedResponse()?.content || []);
  totalElements = computed(() => this.pagedResponse()?.totalElements || 0);

  statusEnum = AppointmentStatus;
  
  scheduledCount = computed(() => this.summary()?.scheduled || 0);
  checkedInCount = computed(() => this.summary()?.checkedIn || 0);
  inConsultationCount = computed(() => this.summary()?.inConsultation || 0);
  completedCount = computed(() => this.summary()?.completed || 0);
  totalVisitsCount = computed(() => this.summary()?.total || 0);
  
  userRole = computed(() => this.authService.currentUser()?.role || null);
  
  pageLead = computed(() => {
    const role = this.userRole();
    if (role === 'DOCTOR') return 'Track only the patients assigned to you and start each consultation from here.';
    if (role === 'RECEPTIONIST') return 'Manage arrivals, move patients into the doctor queue, and keep the OPD flowing.';
    return 'View and manage all medical consultations.';
  });

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private router: Router,
    private billingService: BillingService,
    private excelExportService: ExcelExportService,
  ) {}

  ngOnInit(): void {
    if (this.authService.getUserRole() === 'RECEPTIONIST') {
      this.selectedStatusFilter.set(AppointmentStatus.SCHEDULED);
    }
    this.loadSummary();
    this.loadAppointments();
  }

  loadSummary(): void {
    this.appointmentService.getSummary().subscribe({
      next: (res: ApiResponse<AppointmentSummary>) => this.summary.set(res.data)
    });
  }

  ngOnDestroy(): void {}

  loadAppointments(): void {
    this.isLoading.set(true);
    
    const params = {
      page: this.page(),
      size: this.size(),
      status: this.selectedStatusFilter() === 'ALL' ? undefined : this.selectedStatusFilter() as AppointmentStatus,
    };

    this.appointmentService.search(params).subscribe({
      next: (res: ApiResponse<PagedResponse<Appointment>>) => {
        this.pagedResponse.set(res.data);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  onPageChange(event: any): void {
    this.page.set(event.page);
    this.size.set(event.rows);
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
    this.isLoading.set(true);
    this.billingService.generateFromAppointment(appointmentId).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/billing']);
      },
      error: () => this.isLoading.set(false),
    });
  }

  exportToExcel(): void {
    const dataToExport = this.appointments().map((app) => ({
      'Patient Name': app.patientName,
      'Doctor Name': app.doctorName,
      Department: app.department.replace('_', ' '),
      'Appointment Date': new Date(app.appointmentTime).toLocaleDateString(),
      'Appointment Time': new Date(app.appointmentTime).toLocaleTimeString(),
      Status: app.status,
      Reason: app.reason || 'N/A',
    }));

    this.excelExportService.exportAsExcelFile(dataToExport, 'Appointments_Export');
  }

  canManageAppointment(): boolean {
    const role = this.userRole();
    return role === 'ADMIN' || role === 'RECEPTIONIST';
  }

  setStatusFilter(filter: 'ALL' | AppointmentStatus): void {
    this.selectedStatusFilter.set(filter);
    this.page.set(0); // Reset to first page on filter change
    this.loadAppointments();
  }

  getStatusClass(status: string): string {
    return `status-${status.toLowerCase()}`;
  }

  getWorkflowLabel(appointment: Appointment): string {
    const role = this.userRole();
    switch (appointment.status) {
      case AppointmentStatus.SCHEDULED:
        return role === 'DOCTOR' ? 'Waiting for reception check-in' : 'Scheduled and awaiting arrival';
      case AppointmentStatus.CHECKED_IN:
        return role === 'DOCTOR' ? 'Ready to start consultation' : 'Vitals and doctor handoff pending';
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
    const role = this.userRole();
    return (
      appointment.status === AppointmentStatus.CHECKED_IN && (role === 'DOCTOR' || role === 'ADMIN')
    );
  }
}
