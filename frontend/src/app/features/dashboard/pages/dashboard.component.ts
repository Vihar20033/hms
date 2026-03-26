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
import { User } from '../../../core/models/auth.models';
import { ApiResponse, DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AdminService, HealthResponse } from '../../../core/services/admin.service';
import { HeaderComponent } from '../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../shared/components/layout/sidebar/sidebar.component';

Chart.register(...registerables);

interface QuickAction {
  label: string;
  link: string;
  icon: string;
}

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
  @ViewChild('patientChart') patientChartCanvas!: ElementRef<HTMLCanvasElement>;

  summary: DashboardSummary | null = null;
  isLoading = true;
  healthStatus: HealthResponse | null = null;

  currentUser: User | null = null;
  role = '';
  isAdminOrStaff = false;

  chart: Chart | null = null;
  quickActions: QuickAction[] = [];

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService,
    private adminService: AdminService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
      this.role = user?.role || '';
      this.isAdminOrStaff = ['ADMIN', 'RECEPTIONIST'].includes(this.role);
      this.quickActions = this.buildQuickActions();
      
      if (this.role === 'ADMIN') {
        this.loadHealth();
      }
    });

    this.loadSummary();
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
            this.initChart(this.summary);
          }
        }, 100);
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  loadHealth(): void {
    this.adminService.getHealth().subscribe({
      next: (health) => {
        this.healthStatus = health;
        this.cdr.markForCheck();
      },
      error: () => {
        this.healthStatus = { status: 'DOWN' };
        this.cdr.markForCheck();
      }
    });
  }

  ngAfterViewInit(): void {
    if (this.summary) {
      this.initChart(this.summary);
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  private initChart(data: DashboardSummary): void {
    const canvas = this.patientChartCanvas?.nativeElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    if (this.chart) {
      this.chart.destroy();
    }

    const stats: WeeklyStatistics[] = data.weeklyStats || [];
    const labels = stats.map((s) => s.day);
    const appointmentData = stats.map((s) => s.appointments || 0);
    const patientData = stats.map((s) => s.patients || 0);

    this.chart = new Chart(ctx, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Appointments',
            data: appointmentData,
            borderColor: '#6366f1',
            tension: 0.4,
            fill: true,
          },
          {
            label: 'New Patients',
            data: patientData,
            borderColor: '#10b981',
            tension: 0.4,
            fill: true,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { beginAtZero: true }
        }
      },
    });
  }

  private buildQuickActions(): QuickAction[] {
    if (this.role === 'DOCTOR') {
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

