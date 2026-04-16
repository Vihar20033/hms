import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { Prescription, PrescriptionRequest } from '../models/prescription.models';

@Injectable({
  providedIn: 'root',
})
export class PrescriptionService {
  private apiUrl = `${environment.apiUrl}/prescriptions`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(this.apiUrl);
  }

  getSlice(page = 0, size = 20, query = ''): Observable<ApiResponse<SliceResponse<Prescription>>> {
    const params: Record<string, string | number> = { page, size };
    if (query.trim()) params['query'] = query.trim();

    return this.http.get<ApiResponse<SliceResponse<Prescription>>>(`${this.apiUrl}/slice`, {
      params,
    });
  }

  create(prescription: PrescriptionRequest): Observable<ApiResponse<Prescription>> {
    return this.http.post<ApiResponse<Prescription>>(this.apiUrl, prescription, {
      headers: new HttpHeaders({
        'X-Idempotency-Key': this.createIdempotencyKey('prescription-create'),
      }),
    });
  }

  getById(id: number): Observable<ApiResponse<Prescription>> {
    return this.http.get<ApiResponse<Prescription>>(`${this.apiUrl}/${id}`);
  }

  getByPatientId(patientId: number): Observable<ApiResponse<Prescription[]>> {
    return this.http.get<ApiResponse<Prescription[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  private createIdempotencyKey(prefix: string): string {
    const randomPart = typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function'
      ? crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
    return `${prefix}-${randomPart}`;
  }
}
