import { Injectable } from '@angular/core';

import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { Patient, PatientRequest } from '../models/patient.models';

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  private apiUrl = `${environment.apiUrl}/patients`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Patient[]>> {
    return this.http.get<ApiResponse<Patient[]>>(this.apiUrl);
  }

  getById(id: number): Observable<ApiResponse<Patient>> {
    return this.http.get<ApiResponse<Patient>>(`${this.apiUrl}/${id}`);
  }

  create(patient: PatientRequest): Observable<ApiResponse<Patient>> {
    return this.http.post<ApiResponse<Patient>>(this.apiUrl, patient);
  }

  update(id: number, patient: PatientRequest): Observable<ApiResponse<Patient>> {
    return this.http.put<ApiResponse<Patient>>(`${this.apiUrl}/${id}`, patient);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
