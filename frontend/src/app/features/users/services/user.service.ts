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

  getSlice(page = 0, size = 25): Observable<ApiResponse<SliceResponse<User>>> {
    return this.http.get<ApiResponse<SliceResponse<User>>>(`${this.apiUrl}/slice`, {
      params: { page, size },
    });
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}






