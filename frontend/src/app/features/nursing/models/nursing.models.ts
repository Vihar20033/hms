export enum TriagePriority {
  ROUTINE = 'ROUTINE',
  URGENT = 'URGENT',
  EMERGENCY = 'EMERGENCY',
}

export interface NurseTriageRequest {
  patientId: number;
  appointmentId?: number;
  temperatureCelsius?: number;
  pulse?: number;
  spo2?: number;
  bloodPressure?: string;
  weightKg?: number;
  priority?: TriagePriority;
  notes?: string;
}

