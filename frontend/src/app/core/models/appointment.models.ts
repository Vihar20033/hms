export enum AppointmentStatus {
  SCHEDULED = 'SCHEDULED',
  CHECKED_IN = 'CHECKED_IN',
  CONFIRMED = 'CONFIRMED',
  IN_CONSULTATION = 'IN_CONSULTATION',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export enum Department {
  GENERAL_MEDICINE = 'GENERAL_MEDICINE',
  CARDIOLOGY = 'CARDIOLOGY',
  DERMATOLOGY = 'DERMATOLOGY',
  PEDIATRICS = 'PEDIATRICS',
  ORTHOPEDICS = 'ORTHOPEDICS',
  GYNECOLOGY = 'GYNECOLOGY',
  OPHTHALMOLOGY = 'OPHTHALMOLOGY',
  ENT = 'ENT',
  PSYCHIATRY = 'PSYCHIATRY',
  ONCOLOGY = 'ONCOLOGY',
  EMERGENCY = 'EMERGENCY',
}

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  doctorId?: number;
  doctorName?: string;
  department: Department;
  appointmentTime: string;
  status: AppointmentStatus;
  reason: string;
  notes?: string;
  tokenNumber?: string;
  isEmergency: boolean;
  hasPrescription: boolean;
}

export interface AppointmentRequest {
  patientId: number;
  doctorId?: number;
  department: Department;
  appointmentDate: string; 
  appointmentTime: string; 
  reason: string;
  notes?: string;
  isEmergency: boolean;
}

export interface AppointmentSummary {
  total: number;
  scheduled: number;
  checkedIn: number;
  inConsultation: number;
  completed: number;
  cancelled: number;
}
