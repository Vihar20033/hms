import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/common.models';

export interface HealthResponse {
  status: 'UP' | 'DOWN';
  components?: {
    db?: { status: string; details?: any };
    diskSpace?: { status: string; details?: any };
    ping?: { status: string };
  };
}

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.apiUrl.replace('/api/v1', '')}/actuator/health`);
  }

  restorePatient(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/admin/system/restore/patient/${id}`, {});
  }

  restoreDoctor(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/admin/system/restore/doctor/${id}`, {});
  }

  restoreUser(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/admin/system/restore/user/${id}`, {});
  }

  restoreBilling(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/admin/system/restore/billing/${id}`, {});
  }
}
