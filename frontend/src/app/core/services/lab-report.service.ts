import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, timer } from 'rxjs';
import { retry, timeout } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/common.models';
import { LabReportRequest, LabReportResponse } from '../models/lab-report.models';

@Injectable({ providedIn: 'root' })
export class LabReportService {
  private apiUrl = `${environment.apiUrl}/lab-tests`;

  constructor(private http: HttpClient) {}

  createReport(testId: string, report: LabReportRequest): Observable<ApiResponse<LabReportResponse>> {
    return this.http.post<ApiResponse<LabReportResponse>>
    (`${this.apiUrl}/${testId}/report`, report).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }
  updateReport(reportId: string, report: LabReportRequest): Observable<ApiResponse<LabReportResponse>> {
    return this.http.put<ApiResponse<LabReportResponse>>(`${this.apiUrl}/report/${reportId}`, report).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }

  getReportByTestId(testId: string): Observable<ApiResponse<LabReportResponse>> {
    return this.http.get<ApiResponse<LabReportResponse>>(`${this.apiUrl}/${testId}/report`).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }

  getReportById(reportId: string): Observable<ApiResponse<LabReportResponse>> {
    return this.http.get<ApiResponse<LabReportResponse>>(`${this.apiUrl}/report/${reportId}`).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }

  getAllReports(): Observable<ApiResponse<LabReportResponse[]>> {
    return this.http.get<ApiResponse<LabReportResponse[]>>(`${this.apiUrl}/reports/list`).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }

  deleteReport(reportId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/report/${reportId}`).pipe(
      retry({
        count: 3,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }

  downloadReportPDF(testId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${testId}/report/pdf`, { responseType: 'blob' }).pipe(
      retry({
        count: 2,
        delay: (error, retryCount) => timer(Math.pow(2, retryCount) * 1000),
      }),
      timeout(10000),
    );
  }
}
