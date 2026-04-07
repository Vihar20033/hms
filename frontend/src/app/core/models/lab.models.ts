export enum LabOrderStatus {
  ORDERED = 'ORDERED',
  SAMPLE_COLLECTED = 'SAMPLE_COLLECTED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

export interface LabOrder {
  id: number;
  patientId: number;
  patientName: string;
  appointmentId?: number;
  testName: string;
  status: LabOrderStatus;
  resultSummary?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
}
