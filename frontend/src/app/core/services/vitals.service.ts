import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vitals, VitalsRequest } from '../models/clinical.models';
import { ApiResponse } from '../models/common.models';

@Injectable({
  providedIn: 'root',
})  
export class VitalsService {
  private apiUrl = `${environment.apiUrl}/vitals`;

  constructor(private http: HttpClient) {}

  recordVitals(vitals: VitalsRequest): Observable<ApiResponse<Vitals>> {
    return this.http.post<ApiResponse<Vitals>>(this.apiUrl, vitals);
  }

  updateVitals(id: number, vitals: VitalsRequest): Observable<ApiResponse<Vitals>> {
    return this.http.put<ApiResponse<Vitals>>(`${this.apiUrl}/${id}`, vitals);
  }

  getByAppointment(appointmentId: number): Observable<ApiResponse<Vitals>> {
    return this.http.get<ApiResponse<Vitals>>(`${this.apiUrl}/appointment/${appointmentId}`);
  }

  getAllToday(): Observable<ApiResponse<Vitals[]>> {
    return this.http.get<ApiResponse<Vitals[]>>(`${this.apiUrl}/today`);
  }

  getByPatientId(patientId: number): Observable<ApiResponse<Vitals[]>> {
    return this.http.get<ApiResponse<Vitals[]>>(`${this.apiUrl}/patient/${patientId}`);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
