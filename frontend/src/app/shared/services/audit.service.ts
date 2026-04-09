import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EntityRevision<T = any> {
  revisionId: number;
  revisionDate: string;
  revisionType: 'ADD' | 'MOD' | 'DEL';
  entity: T;
}

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private apiUrl = `${environment.apiUrl}/audit/history`;

  constructor(private http: HttpClient) {}

  getEntityHistory(entityType: string, id: number): Observable<EntityRevision[]> {
    return this.http.get<EntityRevision[]>(`${this.apiUrl}/${entityType}/${id}`);
  }
}
