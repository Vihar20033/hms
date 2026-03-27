import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Billing, BillingRequest, PaymentStatus } from '../models/billing.models';
import { ApiResponse } from '../models/common.models';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private apiUrl = `${environment.apiUrl}/billings`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Billing[]>> {
    return this.http.get<ApiResponse<Billing[]>>(this.apiUrl);
  }

  getById(id: number): Observable<ApiResponse<Billing>> {
    return this.http.get<ApiResponse<Billing>>(`${this.apiUrl}/${id}`);
  }

  getByPatient(patientId: number): Observable<ApiResponse<Billing[]>> {
    return this.http.get<ApiResponse<Billing[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  create(billing: BillingRequest): Observable<ApiResponse<Billing>> {
    return this.http.post<ApiResponse<Billing>>(this.apiUrl, billing);
  }

  updateStatus(id: number, status: PaymentStatus): Observable<ApiResponse<Billing>> {
    return this.http.patch<ApiResponse<Billing>>(`${this.apiUrl}/${id}/status`, null, {
      params: { status },
    });
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  generateFromAppointment(appointmentId: number): Observable<ApiResponse<Billing>> {
    return this.http.post<ApiResponse<Billing>>(`${this.apiUrl}/generate/appointment/${appointmentId}`, {});
  }

  getPreviewFromAppointment(appointmentId: number): Observable<ApiResponse<Billing>> {
    return this.http.get<ApiResponse<Billing>>(`${this.apiUrl}/preview-appointment/${appointmentId}`);
  }
}
