import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { PatientPortalSummary } from '../models/patient-portal.models';

@Injectable({ providedIn: 'root' })
export class PatientPortalService {
  private apiUrl = `${environment.apiUrl}/patient-portal`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<ApiResponse<PatientPortalSummary>> {
    return this.http.get<ApiResponse<PatientPortalSummary>>(`${this.apiUrl}/summary`);
  }
}
