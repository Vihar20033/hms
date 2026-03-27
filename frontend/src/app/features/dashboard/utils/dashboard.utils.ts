import { Chart, ChartConfiguration } from 'chart.js';
import { Appointment, AppointmentStatus } from '../../../core/models/appointment.models';
import { DashboardSummary, WeeklyStatistics } from '../../../core/models/common.models';

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

  if (role === 'RECEPTIONIST' || role === 'ADMIN' || role === 'NURSE') {
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

export function getDashboardWorkflowLabel(appointment: Appointment): string {
  if (appointment.status === AppointmentStatus.SCHEDULED) return 'Awaiting Arrival';
  if (appointment.status === AppointmentStatus.CHECKED_IN) return 'In Dr. Queue';
  if (appointment.status === AppointmentStatus.IN_CONSULTATION) return 'Active Now';
  if (appointment.status === AppointmentStatus.COMPLETED) return 'Visit Ended';
  return '-';
}

export function createDashboardChart(ctx: CanvasRenderingContext2D, data: DashboardSummary): Chart {
  const stats: WeeklyStatistics[] = data.weeklyStats || [];
  const labels = stats.map((s) => s.day);
  const appointmentData = stats.map((s) => s.appointments || 0);
  const patientData = stats.map((s) => s.patients || 0);

  const config: ChartConfiguration = {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          type: 'bar',
          label: 'Appointments',
          data: appointmentData,
          backgroundColor: '#6366f188',
          borderColor: '#6366f1',
          borderWidth: 1,
          borderRadius: 4,
          barThickness: 32,
        },
        {
          type: 'line',
          label: 'New Patients',
          data: patientData,
          borderColor: '#10b981',
          backgroundColor: '#10b981',
          borderWidth: 3,
          pointRadius: 6,
          pointBackgroundColor: '#fff',
          tension: 0.4,
          fill: false,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top' },
        tooltip: { mode: 'index', intersect: false },
      },
      scales: {
        y: { beginAtZero: true, grid: { color: '#f3f4f6' } },
        x: { grid: { display: false } },
      },
    },
  };

  return new Chart(ctx, config);
}
