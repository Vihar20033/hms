import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, SliceResponse } from '../models/common.models';
import { InventoryTransaction, Medicine, MedicineRequest } from '../models/pharmacy.models';

@Injectable({ providedIn: 'root' })
export class PharmacyService {
  private apiUrl = `${environment.apiUrl}/medicines`;
  private inventoryUrl = `${environment.apiUrl}/pharmacy/inventory-log`;

  constructor(private http: HttpClient) {}

  getInventoryLog(): Observable<ApiResponse<InventoryTransaction[]>> {
    return this.http.get<ApiResponse<InventoryTransaction[]>>(this.inventoryUrl);
  }

  getAll(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(this.apiUrl);
  }

  getSlice(page = 0, size = 25): Observable<ApiResponse<SliceResponse<Medicine>>> {
    return this.http.get<ApiResponse<SliceResponse<Medicine>>>(`${this.apiUrl}/slice`, {
      params: { page, size },
    });
  }

  getActive(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(`${this.apiUrl}/active`);
  }

  getLowStock(): Observable<ApiResponse<Medicine[]>> {
    return this.http.get<ApiResponse<Medicine[]>>(`${this.apiUrl}/low-stock`);
  }

  getById(id: number): Observable<ApiResponse<Medicine>> {
    return this.http.get<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`);
  }

  create(medicine: MedicineRequest): Observable<ApiResponse<Medicine>> {
    return this.http.post<ApiResponse<Medicine>>(this.apiUrl, medicine);
  }

  update(id: number, medicine: MedicineRequest): Observable<ApiResponse<Medicine>> {
    return this.http.put<ApiResponse<Medicine>>(`${this.apiUrl}/${id}`, medicine);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }

  restock(id: number, quantity: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(`${this.apiUrl}/${id}/restock`, { quantity });
  }

}
