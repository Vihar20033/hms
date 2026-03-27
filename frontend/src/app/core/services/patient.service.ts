import { Injectable } from '@angular/core';

import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { Patient, PatientRequest, PatientSlice } from '../models/patient.models';

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  private apiUrl = `${environment.apiUrl}/patients`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<ApiResponse<Patient[]>> {
    return this.http.get<ApiResponse<Patient[]>>(`${this.apiUrl}/all`);
  }

  search(
    name?: string,
    email?: string,
    bloodGroup?: string,
    urgencyLevel?: string,
    page = 0,
    size = 10,
  ): Observable<ApiResponse<PatientSlice>> {
    let params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    if (name) params = params.set('name', name);
    if (email) params = params.set('email', email);
    if (bloodGroup) params = params.set('bloodGroup', bloodGroup);
    if (urgencyLevel) params = params.set('urgencyLevel', urgencyLevel);

    return this.http.get<ApiResponse<PatientSlice>>(this.apiUrl, { params });
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
