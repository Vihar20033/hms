import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer } from 'rxjs';
import { retry, timeout } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Appointment, AppointmentRequest, AppointmentStatus, AppointmentSummary } from '../models/appointment.models';
import { ApiResponse, PagedResponse } from '../models/common.models';

@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  
  private apiUrl = `${environment.apiUrl}/appointments`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<ApiResponse<AppointmentSummary>> {
    return this.http
      .get<ApiResponse<AppointmentSummary>>(`${this.apiUrl}/summary`)
      .pipe(retry({ count: 2, delay: 1000 }), timeout(8000));
  }

  search(params: {
    page?: number;
    size?: number;
    sort?: string;
    doctorId?: string;
    patientId?: string;
    status?: AppointmentStatus;
    department?: string;
    start?: string;
    end?: string;
    isEmergency?: boolean;
  }): Observable<ApiResponse<PagedResponse<Appointment>>> {
    const httpParams: any = {};
    Object.keys(params).forEach(key => {
      if ((params as any)[key] !== undefined && (params as any)[key] !== null) {
        httpParams[key] = (params as any)[key].toString();
      }
    });

    return this.http
      .get<ApiResponse<PagedResponse<Appointment>>>(this.apiUrl, { params: httpParams })
      .pipe(retry({ count: 2, delay: 1000 }), timeout(12000));
  }

  getById(id: string): Observable<ApiResponse<Appointment>> {
    return this.http
      .get<ApiResponse<Appointment>>(`${this.apiUrl}/${id}`)
      .pipe(retry({ count: 2, delay: 1000 }), timeout(10000));
  }

  getByPatientId(patientId: string): Observable<ApiResponse<PagedResponse<Appointment>>> {
    return this.search({ patientId });
  }

  create(appointment: AppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http
      .post<ApiResponse<Appointment>>(this.apiUrl, appointment)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  update(id: string, appointment: AppointmentRequest): Observable<ApiResponse<Appointment>> {
    return this.http
      .put<ApiResponse<Appointment>>(`${this.apiUrl}/${id}`, appointment)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  updateStatus(id: string, status: AppointmentStatus): Observable<ApiResponse<Appointment>> {
    return this.http
      .patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/status`, null, {
        params: { status },
      })
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  checkIn(id: string): Observable<ApiResponse<Appointment>> {
    return this.http
      .patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/check-in`, null)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  startConsultation(id: string): Observable<ApiResponse<Appointment>> {
    return this.http
      .patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/start`, null)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  completeConsultation(id: string): Observable<ApiResponse<Appointment>> {
    return this.http
      .patch<ApiResponse<Appointment>>(`${this.apiUrl}/${id}/complete`, null)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }

  delete(id: string): Observable<ApiResponse<void>> {
    return this.http
      .delete<ApiResponse<void>>(`${this.apiUrl}/${id}`)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }
}
