import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { Doctor, DoctorOnboardingResponse, DoctorRegistrationRequest } from '../models/doctor.models';

@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private apiUrl = `${environment.apiUrl}/doctors`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Doctor[]>> {
    return this.http.get<ApiResponse<Doctor[]>>(this.apiUrl);
  }

  getSlice(page = 0, size = 20, query = ''): Observable<ApiResponse<SliceResponse<Doctor>>> {
    const params: Record<string, string | number> = { page, size };
    if (query.trim()) {
      params['query'] = query.trim();
    }

    return this.http.get<ApiResponse<SliceResponse<Doctor>>>(`${this.apiUrl}/slice`, {
      params,
    });
  }

  getById(id: number): Observable<ApiResponse<Doctor>> {
    return this.http.get<ApiResponse<Doctor>>(`${this.apiUrl}/${id}`);
  }

  getByDepartment(dept: string): Observable<ApiResponse<Doctor[]>> {
    return this.http.get<ApiResponse<Doctor[]>>(`${this.apiUrl}/department/${dept}`);
  }

  register(doctor: DoctorRegistrationRequest): Observable<ApiResponse<DoctorOnboardingResponse>> {
    return this.http.post<ApiResponse<DoctorOnboardingResponse>>(this.apiUrl, doctor);
  }

  update(id: number, doctor: Partial<DoctorRegistrationRequest>): Observable<ApiResponse<Doctor>> {
    return this.http.put<ApiResponse<Doctor>>(`${this.apiUrl}/${id}`, doctor);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  getAppointmentCount(id: number): Observable<ApiResponse<number>> {
    return this.http.get<ApiResponse<number>>(`${this.apiUrl}/${id}/appointment-count`);
  }
}
