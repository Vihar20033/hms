import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { LabOrder, LabOrderStatus } from '../models/lab.models';

@Injectable({ providedIn: 'root' })
export class LabService {
  private apiUrl = `${environment.apiUrl}/lab/orders`;

  constructor(private http: HttpClient) {}

  getAll(status?: LabOrderStatus): Observable<ApiResponse<LabOrder[]>> {
    return this.http.get<ApiResponse<LabOrder[]>>(this.apiUrl, {
      params: status ? { status } : {},
    });
  }
}
