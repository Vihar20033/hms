/**
 * Models for Elasticsearch admin functionality
 */

export interface ReindexStatus {
  patientCount: number;
  doctorCount: number;
  appointmentCount: number;
  prescriptionCount: number;
  totalCount: number;
  startTime: string;
  endTime: string;
  status: 'SUCCESS' | 'FAILED' | 'PARTIAL' | 'IN_PROGRESS';
  errorMessage?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
