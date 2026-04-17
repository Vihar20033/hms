import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { ApiResponse, SliceResponse } from '../../../../core/models/common.models';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { InventoryTransaction } from '../../models/pharmacy.models';
import { PharmacyService } from '../../services/pharmacy.service';

@Component({
  selector: 'app-inventory-log',
  standalone: true,
  imports: [CommonModule, TableModule, InputTextModule, SidebarComponent, HeaderComponent],
  templateUrl: './inventory-log.component.html',
  styleUrl: './inventory-log.component.scss',
})
export class InventoryLogComponent implements OnInit {
  transactions: InventoryTransaction[] = [];
  filteredTransactions: InventoryTransaction[] = [];
  isLoading = true;
  currentPage = 0;
  pageSize = 20;
  isFirstPage = true;
  hasNextPage = false;

  constructor(private pharmacyService: PharmacyService) {}

  ngOnInit(): void {
    this.loadTransactions(0);
  }

  loadTransactions(page = 0): void {
    this.isLoading = true;
    this.currentPage = Math.max(page, 0);

    this.pharmacyService.getInventoryLog(this.currentPage, this.pageSize).subscribe({
      next: (res: ApiResponse<SliceResponse<InventoryTransaction>>) => {
        this.transactions = Array.isArray(res.data?.content) ? res.data.content : [];
        this.isFirstPage = !!res.data?.first;
        this.hasNextPage = !!res.data?.hasNext;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  applyFilter(): void {
    this.filteredTransactions = Array.isArray(this.transactions) ? this.transactions : [];
  }

  previousPage(): void {
    if (this.isLoading || this.isFirstPage) return;
    this.loadTransactions(this.currentPage - 1);
  }

  nextPage(): void {
    if (this.isLoading || !this.hasNextPage) return;
    this.loadTransactions(this.currentPage + 1);
  }
}
