import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { AuditLog } from '../models/audit.models';
import { environment } from '../../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuditLogService {
  private readonly apiUrl = `${environment.apiUrl}/audit-logs`;

  constructor(private http: HttpClient) {}

  getSlice(page = 0, size = 20, query = ''): Observable<ApiResponse<SliceResponse<AuditLog>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    const normalizedQuery = query.trim();
    if (normalizedQuery) {
      params = params.set('query', normalizedQuery);
    }

    return this.http.get<ApiResponse<SliceResponse<AuditLog>>>(`${this.apiUrl}/slice`, { params });
  }
}






