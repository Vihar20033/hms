import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Chart, registerables } from 'chart.js';
import { ApiResponse, DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { HeaderComponent } from '../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../shared/components/layout/sidebar/sidebar.component';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('patientChart') patientChartCanvas!: ElementRef;

  summary: DashboardSummary | null = null;
  isLoading = true;
  chart: any;
  currentUser$ = this.authService.currentUser$;
  isAdminOrStaff = false;
  role = '';
  quickActions: Array<{ label: string; link: string; icon: string }> = [];
  private chartPending = false;

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.role = role || '';
    this.isAdminOrStaff = role === 'ADMIN' || role === 'RECEPTIONIST';
    this.quickActions = this.buildQuickActions();

    this.dashboardService.getSummary().subscribe({
      next: (res: ApiResponse<DashboardSummary>) => {
        this.summary = res.data;
        this.isLoading = false;
        this.chartPending = true;
        this.tryInitChart();
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  ngAfterViewInit(): void {
    this.tryInitChart();
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  private buildQuickActions(): Array<{
    label: string;
    link: string;
    icon: string;
  }> {
    if (this.role === 'DOCTOR') {
      return [
        {
          label: 'Open Queue',
          link: '/appointments',
          icon: 'ri-stethoscope-line',
        },
        {
          label: 'Prescriptions',
          link: '/prescriptions',
          icon: 'ri-file-list-3-line',
        },
      ];
    }

    return [
      {
        label: 'Register Patient',
        link: '/patients/register',
        icon: 'ri-user-add-line',
      },
    ];
  }

  private tryInitChart(): void {
    if (
      !this.chartPending ||
      !this.summary ||
      !this.patientChartCanvas?.nativeElement
    ) {
      return;
    }

    this.chartPending = false;
    this.initChart();
  }

  private initChart(): void {
    if (
      !this.summary ||
      !this.patientChartCanvas?.nativeElement
    )
      return;

    const ctx = this.patientChartCanvas.nativeElement.getContext('2d');

    if (!ctx) return;

    if (this.chart) {
      this.chart.destroy();
    }

    // ── 1. Weekly Patient Flow Line Chart ─────────────────────────────────
    const labels = this.summary.weeklyStats.map((s: WeeklyStatistics) => s.day);
    const appointmentData = this.summary.weeklyStats.map((s: WeeklyStatistics) => s.appointments);
    const patientData = this.summary.weeklyStats.map((s: WeeklyStatistics) => s.patients);

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Appointments',
            data: appointmentData,
            borderColor: '#0f6cbd',
            backgroundColor: 'rgba(15, 108, 189, 0.12)',
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#0f6cbd',
          },
          {
            label: 'New Patients',
            data: patientData,
            borderColor: '#1b9aaa',
            backgroundColor: 'rgba(27, 154, 170, 0.12)',
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#1b9aaa',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        resizeDelay: 150,
        animation: false,
        plugins: {
          legend: {
            position: 'top',
            labels: {
              usePointStyle: true,
              padding: 20,
              font: { family: "'Manrope', sans-serif", size: 12 },
            },
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: { display: true, color: 'rgba(0,0,0,0.05)' },
            ticks: { stepSize: 1, font: { family: "'Manrope', sans-serif" } },
          },
          x: {
            grid: { display: false },
            ticks: { font: { family: "'Manrope', sans-serif" } },
          },
        },
      },
    });
  }
}
