import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuditLog } from '../models/audit.models';
import { ApiResponse, SliceResponse } from '../models/common.models';

@Injectable({
  providedIn: 'root',
})
export class AuditLogService {
  private readonly apiUrl = `${environment.apiUrl}/audit-logs`;

  constructor(private http: HttpClient) {}

  getSlice(page = 0, size = 25) {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<ApiResponse<SliceResponse<AuditLog>>>(`${this.apiUrl}/slice`, { params });
  }
}
