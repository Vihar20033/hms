import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface HealthResponse {
  status: 'UP' | 'DOWN';
  components?: {
    db?: { status: string; details?: any };
    diskSpace?: { status: string; details?: any };
    ping?: { status: string };
  };
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(`${this.apiUrl.replace('/api/v1', '')}/actuator/health`);
  }
}








