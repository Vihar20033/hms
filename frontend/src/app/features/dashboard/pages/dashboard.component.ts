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
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { TableModule } from 'primeng/table';
import { ApiResponse, DashboardSummary } from '../../../core/models/common.models';
import { HeaderComponent } from '../../../layout/header/header.component';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../shared/services/status-modal.service';
import { Appointment, AppointmentStatus } from '../../appointments/models/appointment.models';
import { AppointmentService } from '../../appointments/services/appointment.service';
import { User } from '../../auth/models/auth.models';
import { AuthService } from '../../auth/services/auth.service';
import { DashboardService } from '../services/dashboard.service';
import {
  QuickAction,
  buildDashboardQuickActions,
  createDailyVisitFlowChart,
  createDashboardChart,
  createDepartmentChart,
  createStockChart,
  getDashboardStatusClass,
} from '../utils/dashboard.utils';

Chart.register(...registerables, ChartDataLabels);

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
  @ViewChild('visitFlowChart') visitFlowChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('stockChart') stockChartCanvas!: ElementRef<HTMLCanvasElement>;

  summary: DashboardSummary | null = null;
  isLoading = true;

  currentUser: User | null = null;
  role = '';

  chart: Chart | null = null;
  deptChart: Chart | null = null;
  visitFlowChart: Chart | null = null;
  stockChart: Chart | null = null;
  quickActions: QuickAction[] = [];

  todayAppointments: Appointment[] = [];
  isAppointmentsLoading = false;
  queuePage = 0;
  queuePageSize = 8;
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
    if (this.role !== 'PATIENT') {
      this.loadTodayAppointments();
    } else {
      this.isAppointmentsLoading = false;
    }
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
    if (this.visitFlowChart) {
      this.visitFlowChart.destroy();
    }
    if (this.stockChart) {
      this.stockChart.destroy();
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

    // 3. Daily Visit Flow Chart
    const visitFlowCanvas = this.visitFlowChartCanvas?.nativeElement;
    if (visitFlowCanvas) {
      const ctx = visitFlowCanvas.getContext('2d');
      if (ctx) {
        if (this.visitFlowChart) this.visitFlowChart.destroy();
        this.visitFlowChart = createDailyVisitFlowChart(ctx, data);
      }
    }

    // 4. Stock Movement Chart
    const stockCanvas = this.stockChartCanvas?.nativeElement;
    if (stockCanvas) {
      const ctx = stockCanvas.getContext('2d');
      if (ctx) {
        if (this.stockChart) this.stockChart.destroy();
        this.stockChart = createStockChart(ctx, data);
      }
    }
  }

  loadTodayAppointments(): void {
    this.isAppointmentsLoading = true;

    this.appointmentService.getTodayAppointments().subscribe({
      next: (res: ApiResponse<Appointment[]>) => {
        this.todayAppointments = res.data;
        this.queuePage = 0;
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

  get pagedTodayAppointments(): Appointment[] {
    const start = this.queuePage * this.queuePageSize;
    return this.todayAppointments.slice(start, start + this.queuePageSize);
  }

  get isQueueFirstPage(): boolean {
    return this.queuePage === 0;
  }

  get hasQueueNextPage(): boolean {
    return (this.queuePage + 1) * this.queuePageSize < this.todayAppointments.length;
  }

  previousQueuePage(): void {
    if (this.isAppointmentsLoading || this.isQueueFirstPage) return;
    this.queuePage -= 1;
    this.cdr.markForCheck();
  }

  nextQueuePage(): void {
    if (this.isAppointmentsLoading || !this.hasQueueNextPage) return;
    this.queuePage += 1;
    this.cdr.markForCheck();
  }

  getStatusClass(status: AppointmentStatus): string {
    return getDashboardStatusClass(status);
  }

  formatDepartmentName(department: string): string {
    return department
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }

  getDepartmentTotal(data: DashboardSummary): number {
    return data.departmentStats.reduce((total, item) => total + item.appointmentCount, 0);
  }

  getTopDepartments(data: DashboardSummary) {
    return [...data.departmentStats].sort((a, b) => b.appointmentCount - a.appointmentCount).slice(0, 5);
  }

  getDepartmentPercent(count: number, data: DashboardSummary): number {
    const total = this.getDepartmentTotal(data);
    return total > 0 ? Math.round((count / total) * 100) : 0;
  }

  getScheduledVisits(data: DashboardSummary): number {
    return Math.max(
      (data.todayAppointments || 0) - (data.patientsInQueue || 0) - (data.completedConsultations || 0),
      0,
    );
  }

  getWeeklyVisitTotal(data: DashboardSummary): number {
    return (data.weeklyStats || []).reduce((total, item) => total + (item.appointments || 0), 0);
  }

  getPeakVisitDay(data: DashboardSummary): string {
    const stats = data.weeklyStats || [];
    if (!stats.length) {
      return 'No visits';
    }

    const peak = stats.reduce((highest, item) =>
      (item.appointments || 0) > (highest.appointments || 0) ? item : highest,
    );
    return peak.appointments > 0 ? `${peak.day} (${peak.appointments})` : 'No visits';
  }
}
