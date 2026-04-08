import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Patient } from '../../patients/models/patient.models';
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

  getSlice(page = 0, size = 15): Observable<ApiResponse<SliceResponse<Prescription>>> {
    return this.http.get<ApiResponse<SliceResponse<Prescription>>>(`${this.apiUrl}/slice`, {
      params: { page, size },
    });
  }

  create(prescription: PrescriptionRequest): Observable<ApiResponse<Prescription>> {
    return this.http.post<ApiResponse<Prescription>>(this.apiUrl, prescription);
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
}






