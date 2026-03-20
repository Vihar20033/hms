import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timer } from 'rxjs';
import { retry, timeout } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse, DashboardSummary } from '../models/common.models';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  getSummary(): Observable<ApiResponse<DashboardSummary>> {
    return this.http
      .get<ApiResponse<DashboardSummary>>(`${this.apiUrl}/summary`)
      .pipe(retry({ count: 3, delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000) }), timeout(10000));
  }
}
