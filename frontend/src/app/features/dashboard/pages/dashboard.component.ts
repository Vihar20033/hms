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
  computed,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { ApiResponse, DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AdminService, HealthResponse } from '../../../core/services/admin.service';
import { HeaderComponent } from '../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../shared/components/layout/sidebar/sidebar.component';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    SidebarComponent,
    HeaderComponent,
    RouterLink,
    CurrencyPipe,
    DecimalPipe,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  /** Canvas is always in the DOM (outside @if) so this always resolves. */
  @ViewChild('patientChart') patientChartCanvas!: ElementRef<HTMLCanvasElement>;

  summary = signal<DashboardSummary | null>(null);
  isLoading = signal<boolean>(true);
  healthStatus = signal<HealthResponse | null>(null);

  currentUser = this.authService.currentUser;
  role = computed(() => this.currentUser()?.role || '');
  isAdminOrStaff = computed(() => ['ADMIN', 'RECEPTIONIST'].includes(this.role()));

  chart: Chart | null = null;
  quickActions: Array<{ label: string; link: string; icon: string }> = [];

  private viewInitialized = false;

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.quickActions = this.buildQuickActions();

    this.dashboardService.getSummary().subscribe({
      next: (res: ApiResponse<DashboardSummary>) => {
        this.summary.set(res.data);
        this.isLoading.set(false);
        this.cdr.markForCheck();

        if (this.viewInitialized) {
          this.initChart(res.data);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.cdr.markForCheck();
      },
    });

    if (this.role() === 'ADMIN') {
      this.adminService.getHealth().subscribe({
        next: (health) => {
          this.healthStatus.set(health);
          this.cdr.markForCheck();
        },
        error: () => {
          this.healthStatus.set({ status: 'DOWN' });
          this.cdr.markForCheck();
        }
      });
    }
  }

  ngAfterViewInit(): void {
    this.viewInitialized = true;
    const data = this.summary();
    if (data) {
      // Data already arrived before the view was ready
      this.initChart(data);
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  private initChart(data: DashboardSummary): void {
    const canvas = this.patientChartCanvas?.nativeElement;
    if (!canvas) return;

    try {
      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      if (this.chart) {
        this.chart.destroy();
        this.chart = null;
      }

      const stats: WeeklyStatistics[] = data.weeklyStats ?? [];
      const labels = stats.map((s) => s.day);
      const appointmentData = stats.map((s) => s.appointments ?? 0);
      const patientData = stats.map((s) => s.patients ?? 0);

      this.chart = new Chart(ctx, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: 'Daily Appointments',
              data: appointmentData,
              borderColor: '#6366f1',
              backgroundColor: 'rgba(99, 102, 241, 0.12)',
              tension: 0.4,
              fill: true,
              pointBackgroundColor: '#6366f1',
              pointBorderColor: '#fff',
              pointBorderWidth: 2,
              pointRadius: 5,
              pointHoverRadius: 7,
            },
            {
              label: 'New Patients',
              data: patientData,
              borderColor: '#10b981',
              backgroundColor: 'rgba(16, 185, 129, 0.08)',
              tension: 0.4,
              fill: true,
              pointBackgroundColor: '#10b981',
              pointBorderColor: '#fff',
              pointBorderWidth: 2,
              pointRadius: 5,
              pointHoverRadius: 7,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          animation: { duration: 600, easing: 'easeInOutQuart' },
          plugins: {
            legend: {
              position: 'bottom',
              labels: {
                usePointStyle: true,
                padding: 20,
                font: { size: 12 },
              },
            },
            tooltip: {
              mode: 'index',
              intersect: false,
              padding: 12,
              backgroundColor: 'rgba(15, 23, 42, 0.92)',
              bodySpacing: 6,
              titleFont: { weight: 'bold' },
            },
          },
          interaction: { intersect: false, mode: 'index' },
          scales: {
            y: {
              beginAtZero: true,
              grid: { color: 'rgba(0,0,0,0.06)' },
              ticks: { precision: 0, stepSize: 1 },
            },
            x: {
              grid: { display: false },
            },
          },
        },
      });
    } catch (err) {
      console.error('[DashboardComponent] Chart init failed:', err);
    }
  }

  private buildQuickActions(): Array<{ label: string; link: string; icon: string }> {
    const currentRole = this.role();
    if (currentRole === 'DOCTOR') {
      return [
        { label: 'Open Queue', link: '/appointments', icon: 'ri-stethoscope-line' },
        { label: 'Prescriptions', link: '/prescriptions', icon: 'ri-file-list-3-line' },
      ];
    }
    return [
      { label: 'Register Patient', link: '/patients/register', icon: 'ri-user-add-line' },
    ];
  }
}
