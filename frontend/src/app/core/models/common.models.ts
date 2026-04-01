export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  status: number;
  timestamp: string;
  errorCode?: string;
  path?: string;
}

export interface WeeklyStatistics {
  day: string;
  appointments: number;
  patients: number;
}

export interface DepartmentStatistics {
  department: string;
  appointmentCount: number;
}

export interface DashboardSummary {
  totalPatients: number;
  todayAppointments: number;
  lowStockMedicines: number;
  totalRevenue: number;
  patientsInQueue: number;
  completedConsultations: number;
  totalCompletedConsultations: number;
  weeklyStats: WeeklyStatistics[];
  departmentStats: DepartmentStatistics[];
}
