import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ApiResponse, ReindexStatus } from '../models/elasticsearch-admin.models';

/**
 * Service for Elasticsearch admin operations.
 * Handles reindexing and search management from the frontend.
 */
@Injectable({
  providedIn: 'root',
})
export class ElasticsearchAdminService {
  private apiUrl = `${environment.apiUrl}/admin/search`;

  constructor(private http: HttpClient) {}

  /**
   * Reindex all patients
   */
  reindexPatients(): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/reindex/patients`, {});
  }

  /**
   * Reindex all doctors
   */
  reindexDoctors(): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/reindex/doctors`, {});
  }

  /**
   * Reindex all appointments
   */
  reindexAppointments(): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/reindex/appointments`, {});
  }

  /**
   * Reindex all prescriptions
   */
  reindexPrescriptions(): Observable<ApiResponse<number>> {
    return this.http.post<ApiResponse<number>>(`${this.apiUrl}/reindex/prescriptions`, {});
  }

  /**
   * Perform full reindex of all entities
   */
  reindexAll(): Observable<ApiResponse<ReindexStatus>> {
    return this.http.post<ApiResponse<ReindexStatus>>(`${this.apiUrl}/reindex/all`, {});
  }

  /**
   * Clear all search indices
   */
  clearAllIndices(): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/indices/clear-all`);
  }

  /**
   * Check Elasticsearch health status
   */
  checkHealth(): Observable<ApiResponse<boolean>> {
    return this.http.get<ApiResponse<boolean>>(`${this.apiUrl}/health`);
  }
}
