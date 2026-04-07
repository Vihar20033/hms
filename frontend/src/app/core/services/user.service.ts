import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, SliceResponse } from '../models/common.models';
import { User } from '../models/auth.models';

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
