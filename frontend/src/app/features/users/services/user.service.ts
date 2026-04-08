import { ApiResponse, SliceResponse } from '../../../core/models/common.models';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from '../../auth/models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(this.apiUrl);
  }

  getSlice(page = 0, size = 25, query = ''): Observable<ApiResponse<SliceResponse<User>>> {
    const params: Record<string, string | number> = { page, size };
    if (query.trim()) params['query'] = query.trim();

    return this.http.get<ApiResponse<SliceResponse<User>>>(`${this.apiUrl}/slice`, {
      params,
    });
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}






