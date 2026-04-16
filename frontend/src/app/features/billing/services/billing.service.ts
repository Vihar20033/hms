import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { Billing, BillingRequest, PaymentStatus } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class BillingService {
  private apiUrl = `${environment.apiUrl}/billings`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Billing[]>> {
    return this.http.get<ApiResponse<Billing[]>>(this.apiUrl);
  }

  getSlice(page = 0, size = 20, query = ''): Observable<ApiResponse<SliceResponse<Billing>>> {
    const params: Record<string, string | number> = { page, size };
    if (query.trim()) params['query'] = query.trim();

    return this.http.get<ApiResponse<SliceResponse<Billing>>>(`${this.apiUrl}/slice`, {
      params,
    });
  }

  getById(id: number): Observable<ApiResponse<Billing>> {
    return this.http.get<ApiResponse<Billing>>(`${this.apiUrl}/${id}`);
  }

  getByPatient(patientId: number): Observable<ApiResponse<Billing[]>> {
    return this.http.get<ApiResponse<Billing[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  getMine(): Observable<ApiResponse<Billing[]>> {
    return this.http.get<ApiResponse<Billing[]>>(`${this.apiUrl}/me`);
  }

  create(billing: BillingRequest): Observable<ApiResponse<Billing>> {
    return this.http.post<ApiResponse<Billing>>(this.apiUrl, billing);
  }

  updateStatus(id: number, status: PaymentStatus): Observable<ApiResponse<Billing>> {
    return this.http.patch<ApiResponse<Billing>>(`${this.apiUrl}/${id}/status`, null, {
      params: { status },
    });
  }

  payMine(id: number): Observable<ApiResponse<Billing>> {
    return this.http.patch<ApiResponse<Billing>>(`${this.apiUrl}/${id}/pay`, null);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  generateFromAppointment(appointmentId: number): Observable<ApiResponse<Billing>> {
    return this.http.post<ApiResponse<Billing>>(`${this.apiUrl}/generate/appointment/${appointmentId}`, {}, {
      headers: new HttpHeaders({
        'X-Idempotency-Key': this.createIdempotencyKey('billing-generate'),
      }),
    });
  }

  getPreviewFromAppointment(appointmentId: number): Observable<ApiResponse<Billing>> {
    return this.http.get<ApiResponse<Billing>>(`${this.apiUrl}/preview-appointment/${appointmentId}`);
  }

  private createIdempotencyKey(prefix: string): string {
    const randomPart = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    return `${prefix}-${randomPart}`;
  }
}
