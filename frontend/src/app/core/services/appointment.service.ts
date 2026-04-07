import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Appointment, AppointmentRequest, AppointmentStatus, AppointmentSummary } from '../models/appointment.models';
import { ApiResponse } from '../models/common.models';

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  
  private apiUrl = `${environment.apiUrl}/appointments`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<ApiResponse<AppointmentSummary>> {
    return this.http.get<ApiResponse<AppointmentSummary>>(`${this.apiUrl}/summary`);
  }

  getAll(params?: {
    patientId?: number;
    status?: AppointmentStatus;
  }): Observable<ApiResponse<Appointment[]>> {
    const httpParams: Record<string, string> = {};

    if (params?.patientId !== undefined) {
      httpParams['patientId'] = params.patientId.toString();
    }
    if (params?.status !== undefined) {
      httpParams['status'] = params.status;
    }

    return this.http.get<ApiResponse<Appointment[]>>(this.apiUrl, { params: httpParams });
  }

  getById(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.get<ApiResponse<Appointment>>(`${this.apiUrl}/${id}`);
  }

  getByPatientId(patientId: number): Observable<ApiResponse<Appointment[]>> {
    return this.getAll({ patientId });
  }

  create(appointment: AppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http.post<ApiResponse<Appointment>>(this.apiUrl, appointment);
  }

  update(id: number, appointment: AppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http.put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}`, appointment);
  }

  updateStatus(id: number, status: AppointmentStatus): Observable<ApiResponse<Appointment>> {
    return this.http.patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/status`, null, {
      params: { status },
    });
  }

  checkIn(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/check-in`, null);
  }

  startConsultation(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/start`, null);
  }

  completeConsultation(id: number): Observable<ApiResponse<Appointment>> {
    return this.http.patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/complete`, null);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getTodayAppointments(): Observable<ApiResponse<Appointment[]>> {
    return this.http.get<ApiResponse<Appointment[]>>(`${this.apiUrl}/today`);
  }

  reassign(fromDoctorId: number, toDoctorId: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/reassign`, null, {
      params: { fromDoctorId, toDoctorId }
    });
  }
}
