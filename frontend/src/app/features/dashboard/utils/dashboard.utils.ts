import { Appointment, AppointmentStatus, Department } from '../../appointments/models/appointment.models';
import { Chart, ChartConfiguration, TooltipItem } from 'chart.js';
import { DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';
import { Doctor } from '../../staff/models/doctor.models';
import { map } from 'rxjs/operators';
import { Patient } from '../../patients/models/patient.models';
import { Role, User } from '../../auth/models/auth.models';

export interface QuickAction {
  label: string;
  link: string;
  icon: string;
}

export function buildDashboardQuickActions(role: string): QuickAction[] {
  if (role === 'DOCTOR') {
    return [
      { label: 'Register Patient', link: '/patients/register', icon: 'ri-user-add-line' },
      { label: 'Prescriptions', link: '/prescriptions', icon: 'ri-file-list-3-line' },
    ];
  }

  if (role === 'PHARMACIST') {
    return [
      { label: 'View Prescriptions', link: '/prescriptions', icon: 'ri-file-list-3-line' },
      { label: 'Inventory', link: '/pharmacy/inventory', icon: 'ri-capsule-line' },
    ];
  }

  if (role === 'RECEPTIONIST' || role === 'ADMIN') {
    return [
      { label: 'Register Patient', link: '/patients/register', icon: 'ri-user-add-line' },
      { label: 'Book Appointment', link: '/appointments/book', icon: 'ri-calendar-todo-line' },
    ];
  }

  return [];
}

export function getDashboardStatusClass(status: AppointmentStatus): string {
  const map: Record<string, string> = {
    SCHEDULED: 'status-scheduled',
    CHECKED_IN: 'status-checked-in',
    IN_CONSULTATION: 'status-in-progress',
    COMPLETED: 'status-completed',
    CANCELLED: 'status-cancelled',
  };

  return map[status] || 'status-scheduled';
}

export function createDashboardChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const stats: WeeklyStatistics[] = data.weeklyStats || [];
  const labels = stats.map((s) => s.day);
  const appointmentData = stats.map((s) => s.appointments || 0);
  const patientData = stats.map((s) => s.patients || 0);

  const config: ChartConfiguration<'bar'> = {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Appointments',
          data: appointmentData,
          backgroundColor: 'rgba(37, 99, 235, 0.78)',
          borderColor: '#2563eb',
          borderWidth: 1,
          borderRadius: 8,
          maxBarThickness: 42,
        },
        {
          label: 'New Patients',
          data: patientData,
          backgroundColor: 'rgba(20, 184, 166, 0.78)',
          borderColor: '#14b8a6',
          borderWidth: 1,
          borderRadius: 8,
          maxBarThickness: 42,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      layout: {
        padding: {
          top: 4,
          right: 8,
          bottom: 0,
          left: 0,
        },
      },
      plugins: {
        legend: { 
          position: 'top',
          labels: {
            usePointStyle: true,
            boxWidth: 8,
            boxHeight: 8,
            padding: 14,
            font: { weight: 'bold', size: 12 }
          }
        },
        tooltip: {
          mode: 'index',
          intersect: false,
          padding: 12,
          backgroundColor: 'rgba(15, 23, 42, 0.9)',
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          grace: 1,
          ticks: {
            precision: 0,
            maxTicksLimit: 5,
            padding: 8,
          },
          border: { display: false },
          grid: { color: '#eef2f7' },
        },
        x: { 
          border: { display: false },
          grid: { display: false },
          ticks: {
            padding: 8,
            font: { weight: 'bold', size: 11 },
          }
        },
      },
    },
  };

  return new Chart(ctx, config);
}

export function createDepartmentChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const stats = data.departmentStats || [];
  const labels = stats.map((s) =>
    s.department
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase()),
  );
  const counts = stats.map((s) => s.appointmentCount);

  const config: ChartConfiguration<'doughnut'> = {
    type: 'doughnut',
    data: {
      labels,
      datasets: [
        {
          label: 'Appointments',
          data: counts,
          backgroundColor: [
            'rgba(37, 99, 235, 0.8)',
            'rgba(20, 184, 166, 0.8)',
            'rgba(245, 158, 11, 0.8)',
            'rgba(239, 68, 68, 0.8)',
            'rgba(139, 92, 246, 0.8)',
            'rgba(236, 72, 153, 0.8)',
            'rgba(59, 130, 246, 0.8)',
            'rgba(16, 185, 129, 0.8)',
          ],
          borderColor: '#fff',
          borderWidth: 2,
          hoverOffset: 15,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '70%',
      plugins: {
        legend: {
          display: true,
          position: 'right',
          labels: {
            usePointStyle: true,
            padding: 15,
            font: { weight: 'bold', size: 11 }
          }
        },
        tooltip: {
          padding: 12,
          backgroundColor: 'rgba(15, 23, 42, 0.9)',
          titleFont: { size: 13 },
          bodyFont: { size: 13 },
          callbacks: {
            label: (item: TooltipItem<'doughnut'>) => ` ${item.label}: ${item.raw} visits`,
          },
        },
      },
    },
  };

  return new Chart(ctx, config);
}

export function createDailyVisitFlowChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const completed = data.completedConsultations || 0;
  const waiting = data.patientsInQueue || 0;
  const today = data.todayAppointments || 0;
  const scheduled = Math.max(today - completed - waiting, 0);

  const config: ChartConfiguration<'bar'> = {
    type: 'bar',
    data: {
      labels: ['Scheduled', 'Waiting', 'Completed'],
      datasets: [
        {
          label: 'Visits',
          data: [scheduled, waiting, completed],
          backgroundColor: ['rgba(37, 99, 235, 0.78)', 'rgba(245, 158, 11, 0.78)', 'rgba(16, 185, 129, 0.78)'],
          borderColor: ['#2563eb', '#d97706', '#059669'],
          borderWidth: 1,
          borderRadius: 8,
          maxBarThickness: 56,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      layout: {
        padding: 0,
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          padding: 12,
          backgroundColor: 'rgba(15, 23, 42, 0.9)',
          callbacks: {
            label: (item: TooltipItem<'bar'>) => ` ${item.raw} visits`,
          },
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          grace: 1,
          ticks: {
            precision: 0,
            maxTicksLimit: 4,
            padding: 8,
          },
          border: { display: false },
          grid: { color: '#f1f5f9' },
        },
        x: {
          border: { display: false },
          grid: { display: false },
          ticks: {
            padding: 10,
            font: { weight: 'bold' },
          },
        },
      },
    },
  };

  return new Chart(ctx, config);
}










