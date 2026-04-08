import { ApiResponse } from '../../../../core/models/common.models';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { InputTextModule } from 'primeng/inputtext';
import { InventoryTransaction } from '../../models/pharmacy.models';
import { PharmacyService } from '../../services/pharmacy.service';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { TableModule } from 'primeng/table';

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

  constructor(private pharmacyService: PharmacyService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.isLoading = true;
    this.pharmacyService.getInventoryLog().subscribe({
      next: (res: ApiResponse<InventoryTransaction[]>) => {
        this.transactions = res.data;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

 // Placeholder for filter logic - currently just copies all transactions to filteredTransactions
  applyFilter(): void {
    this.filteredTransactions = this.transactions;
  }
}












