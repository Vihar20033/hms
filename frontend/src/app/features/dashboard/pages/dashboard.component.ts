import { CommonModule, CurrencyPipe, DecimalPipe } from '@angular/common';
import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { TableModule } from 'primeng/table';
import { Appointment, AppointmentStatus } from '../../../core/models/appointment.models';
import { User } from '../../../core/models/auth.models';
import { ApiResponse, DashboardSummary } from '../../../core/models/common.models';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { StatusModalService } from '../../../core/services/status-modal.service';
import { HeaderComponent } from '../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../shared/components/layout/sidebar/sidebar.component';
import {
  QuickAction,
  buildDashboardQuickActions,
  createDashboardChart,
  createDepartmentChart,
  getDashboardStatusClass,
} from '../utils/dashboard.utils';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, CurrencyPipe, DecimalPipe, TableModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('patientChart') patientChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('deptChart') deptChartCanvas!: ElementRef<HTMLCanvasElement>;

  summary: DashboardSummary | null = null;
  isLoading = true;

  currentUser: User | null = null;
  role = '';

  chart: Chart | null = null;
  deptChart: Chart | null = null;
  quickActions: QuickAction[] = [];

  todayAppointments: Appointment[] = [];
  isAppointmentsLoading = false;
  statusEnum = AppointmentStatus;

  constructor(
    private dashboardService: DashboardService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private statusModalService: StatusModalService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe((user) => {
      this.currentUser = user;
      this.role = user?.role || '';
      this.quickActions = buildDashboardQuickActions(this.role);
    });

    this.loadSummary();
    this.loadTodayAppointments();
  }

  loadSummary(): void {
    this.dashboardService.getSummary().subscribe({
      next: (res: ApiResponse<DashboardSummary>) => {
        this.summary = res.data;
        this.isLoading = false;
        this.cdr.markForCheck();

        // Short delay to ensure canvas is in DOM
        setTimeout(() => {
          if (this.summary) {
            this.initCharts(this.summary);
          }
        }, 100);
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  ngAfterViewInit(): void {
    if (this.summary) {
      this.initCharts(this.summary);
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
    if (this.deptChart) {
      this.deptChart.destroy();
    }
  }

  private initCharts(data: DashboardSummary): void {
    // 1. Activity Chart
    const activityCanvas = this.patientChartCanvas?.nativeElement;
    if (activityCanvas) {
      const ctx = activityCanvas.getContext('2d');
      if (ctx) {
        if (this.chart) this.chart.destroy();
        this.chart = createDashboardChart(ctx, data);
      }
    }

    // 2. Department Chart
    const deptCanvas = this.deptChartCanvas?.nativeElement;
    if (deptCanvas) {
      const ctx = deptCanvas.getContext('2d');
      if (ctx) {
        if (this.deptChart) this.deptChart.destroy();
        this.deptChart = createDepartmentChart(ctx, data);
      }
    }
  }

  loadTodayAppointments(): void {
    this.isAppointmentsLoading = true;

    this.appointmentService.getTodayAppointments().subscribe({
      next: (res: ApiResponse<Appointment[]>) => {
        this.todayAppointments = res.data;
        this.isAppointmentsLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isAppointmentsLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  onCheckIn(id: number): void {
    this.appointmentService.checkIn(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Patient Checked In', "The patient is now in the doctor's queue.");
        this.loadTodayAppointments();
        this.loadSummary();
      },
      error: (err) =>
        this.statusModalService.showError('Check-in Failed', err.error?.message || 'Could not process check-in.'),
    });
  }

  onStart(id: number): void {
    this.appointmentService.startConsultation(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Consultation Started', 'The visit session is now active.');
        this.loadTodayAppointments();
        this.loadSummary();
      },
      error: (err) => this.statusModalService.showError('Error', err.error?.message || 'Could not start session.'),
    });
  }

  onComplete(id: number): void {
    this.appointmentService.completeConsultation(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Visit Completed', 'Consultation ended successfully.');
        this.loadTodayAppointments();
        this.loadSummary();
      },
      error: (err) => this.statusModalService.showError('Error', err.error?.message || 'Could not complete visit.'),
    });
  }

  getStatusClass(status: AppointmentStatus): string {
    return getDashboardStatusClass(status);
  }
}
