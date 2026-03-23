import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TableModule } from 'primeng/table';
import { Appointment, AppointmentStatus } from '../../../../core/models/appointment.models';
import { Billing } from '../../../../core/models/billing.models';
import { ApiResponse } from '../../../../core/models/common.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { ExcelExportService } from '../../../../core/services/excel-export.service';

import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-appointment-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, TableModule],
  templateUrl: './appointment-list.component.html',
  styleUrl: './appointment-list.component.scss',
})
export class AppointmentListComponent implements OnInit, OnDestroy {
  appointments: Appointment[] = [];
  filteredAppointments: Appointment[] = [];
  isLoading = true;
  statusEnum = AppointmentStatus;
  userRole: string | null = null;
  selectedStatusFilter: 'ALL' | AppointmentStatus = 'ALL';
  scheduledCount = 0;
  checkedInCount = 0;
  inConsultationCount = 0;
  completedCount = 0;
  pageLead = 'View and manage all medical consultations.';

  constructor(
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private router: Router,
    private billingService: BillingService,
    private excelExportService: ExcelExportService,
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    if (this.userRole === 'RECEPTIONIST') {
      this.selectedStatusFilter = AppointmentStatus.SCHEDULED;
    }
    this.pageLead = this.getPageLead();
    this.loadAppointments();
  }

  ngOnDestroy(): void {}

  loadAppointments(): void {
    this.isLoading = true;
    
    // Pattern: Use getMyAppointments() for Doctors/Patients, getAll() for Staff
    const isClinicalStaff = ['ADMIN', 'RECEPTIONIST', 'NURSE'].includes(this.userRole || '');
    const request = isClinicalStaff 
      ? this.appointmentService.getAll() 
      : this.appointmentService.getMyAppointments();

    request.subscribe({
      next: (res: ApiResponse<Appointment[]>) => {
        this.appointments = res.data || [];
        this.refreshQueueView();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onCheckIn(id: string): void {
    this.appointmentService.checkIn(id).subscribe(() => {
      this.loadAppointments();
    });
  }

  onStart(id: string): void {
    this.appointmentService.startConsultation(id).subscribe(() => {
      this.loadAppointments();
    });
  }

  onComplete(id: string): void {
    this.appointmentService.completeConsultation(id).subscribe(() => {
      this.loadAppointments();
    });
  }

  onEdit(id: string): void {
    this.router.navigate(['/appointments/book'], { queryParams: { appointmentId: id } });
  }

  onDelete(id: string): void {
    if (!confirm('Delete this appointment?')) {
      return;
    }

    this.appointmentService.delete(id).subscribe(() => {
      this.loadAppointments();
    });
  }

  onGenerateBill(appointmentId: string): void {
    this.isLoading = true;
    this.billingService.generateFromAppointment(appointmentId).subscribe({
      next: (_res: ApiResponse<Billing>) => {
        this.isLoading = false;
        this.router.navigate(['/billing']);
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  exportToExcel(): void {
    const dataToExport = this.filteredAppointments.map((app) => ({
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
    return this.userRole === 'ADMIN' || this.userRole === 'RECEPTIONIST';
  }

  setStatusFilter(filter: 'ALL' | AppointmentStatus): void {
    this.selectedStatusFilter = filter;
    this.applyStatusFilter();
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

  private getPageLead(): string {
    if (this.userRole === 'DOCTOR') {
      return 'Track only the patients assigned to you and start each consultation from here.';
    }

    if (this.userRole === 'RECEPTIONIST') {
      return 'Manage arrivals, move patients into the doctor queue, and keep the OPD flowing.';
    }

    if (this.userRole === 'PATIENT') {
      return 'Track your upcoming visits and consultation history.';
    }

    return 'View and manage all medical consultations.';
  }

  private refreshQueueView(): void {
    this.scheduledCount = this.appointments.filter(
      (appointment) => appointment.status === AppointmentStatus.SCHEDULED,
    ).length;
    this.checkedInCount = this.appointments.filter(
      (appointment) => appointment.status === AppointmentStatus.CHECKED_IN,
    ).length;
    this.inConsultationCount = this.appointments.filter(
      (appointment) => appointment.status === AppointmentStatus.IN_CONSULTATION,
    ).length;
    this.completedCount = this.appointments.filter(
      (appointment) => appointment.status === AppointmentStatus.COMPLETED,
    ).length;
    this.applyStatusFilter();
  }

  private applyStatusFilter(): void {
    this.filteredAppointments =
      this.selectedStatusFilter === 'ALL'
        ? [...this.appointments]
        : this.appointments.filter((appointment) => appointment.status === this.selectedStatusFilter);
  }
}
