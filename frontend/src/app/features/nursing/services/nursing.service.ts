import { ApiResponse } from '../../../core/models/common.models';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NurseTriageRequest } from '../models/nursing.models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NursingService {
  private apiUrl = `${environment.apiUrl}/nursing/triage`;

  constructor(private http: HttpClient) {}

  createTriage(request: NurseTriageRequest): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(this.apiUrl, request);
  }
}






