import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { TableModule } from 'primeng/table';
import { Billing, PaymentStatus } from '../../../../core/models/billing.models';
import { getBillingStatusClass } from '../../utils/billing-data.utils';

@Component({
  selector: 'app-billing-table',
  standalone: true,
  imports: [CommonModule, TableModule, DatePipe, DecimalPipe],
  templateUrl: './billing-table.component.html',
  styleUrl: './billing-table.component.scss'
})
export class BillingTableComponent {
  @Input() billings: Billing[] = [];
  @Input() isLoading = false;
  @Input() userRole: string | null = null;
  @Input() exportingId: number | null = null;
  
  @Output() view = new EventEmitter<Billing>();
  @Output() updateStatus = new EventEmitter<{ id: number; status: PaymentStatus }>();
  @Output() exportPdf = new EventEmitter<Billing>();
  @Output() delete = new EventEmitter<number>();

  PaymentStatus = PaymentStatus;

  onView(bill: Billing): void {
    this.view.emit(bill);
  }

  onUpdateStatus(id: number, status: PaymentStatus): void {
    this.updateStatus.emit({ id, status });
  }

  onExportPdf(bill: Billing): void {
    this.exportPdf.emit(bill);
  }

  onDelete(id: number): void {
    this.delete.emit(id);
  }

  getStatusClass(status: PaymentStatus): string {
    return getBillingStatusClass(status);
  }
}
