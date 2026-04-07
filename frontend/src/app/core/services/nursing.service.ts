import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { NurseTriageRequest } from '../models/nursing.models';

@Injectable({ providedIn: 'root' })
export class NursingService {
  private apiUrl = `${environment.apiUrl}/nursing/triage`;

  constructor(private http: HttpClient) {}

  createTriage(request: NurseTriageRequest): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(this.apiUrl, request);
  }
}
