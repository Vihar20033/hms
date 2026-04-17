import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ApiResponse } from '../../../core/models/common.models';
import {
  CreateWorkflowDefinitionRequest,
  StartWorkflowInstanceRequest,
  TransitionWorkflowRequest,
  WorkflowDefinitionResponse,
  WorkflowInstanceResponse,
  WorkflowTaskResponse,
} from '../models/workflow-admin.models';

@Injectable({
  providedIn: 'root',
})
export class WorkflowAdminService {
  private apiUrl = `${environment.apiUrl}/workflows`;

  constructor(private http: HttpClient) {}

  createDefinition(payload: CreateWorkflowDefinitionRequest): Observable<ApiResponse<WorkflowDefinitionResponse>> {
    return this.http.post<ApiResponse<WorkflowDefinitionResponse>>(`${this.apiUrl}/definitions`, payload);
  }

  activateDefinition(
    definitionKey: string,
    versionNumber: number,
  ): Observable<ApiResponse<WorkflowDefinitionResponse>> {
    return this.http.put<ApiResponse<WorkflowDefinitionResponse>>(
      `${this.apiUrl}/definitions/${definitionKey}/versions/${versionNumber}/activate`,
      {},
    );
  }

  listDefinitions(definitionKey?: string): Observable<ApiResponse<WorkflowDefinitionResponse[]>> {
    let params = new HttpParams();
    if (definitionKey?.trim()) {
      params = params.set('definitionKey', definitionKey.trim());
    }

    return this.http.get<ApiResponse<WorkflowDefinitionResponse[]>>(`${this.apiUrl}/definitions`, { params });
  }

  startInstance(payload: StartWorkflowInstanceRequest): Observable<ApiResponse<WorkflowInstanceResponse>> {
    return this.http.post<ApiResponse<WorkflowInstanceResponse>>(`${this.apiUrl}/instances`, payload);
  }

  transitionInstance(
    instanceId: number,
    payload: TransitionWorkflowRequest,
  ): Observable<ApiResponse<WorkflowInstanceResponse>> {
    return this.http.post<ApiResponse<WorkflowInstanceResponse>>(
      `${this.apiUrl}/instances/${instanceId}/transition`,
      payload,
    );
  }

  getInstance(instanceId: number): Observable<ApiResponse<WorkflowInstanceResponse>> {
    return this.http.get<ApiResponse<WorkflowInstanceResponse>>(`${this.apiUrl}/instances/${instanceId}`);
  }

  getInstanceTasks(instanceId: number): Observable<ApiResponse<WorkflowTaskResponse[]>> {
    return this.http.get<ApiResponse<WorkflowTaskResponse[]>>(`${this.apiUrl}/instances/${instanceId}/tasks`);
  }
}
