import { Billing, PaymentStatus } from '../../models/billing.models';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { TableModule } from 'primeng/table';
import { getBillingStatusClass } from '../../utils/billing-data.utils';

@Component({
  selector: 'app-billing-table',
  standalone: true,
  imports: [CommonModule, TableModule, DatePipe, DecimalPipe],
  templateUrl: './billing-table.component.html',
  styleUrl: './billing-table.component.scss'
})
export class BillingTableComponent {
  readonly billings = input<Billing[]>([]);
  readonly isLoading = input(false);
  readonly userRole = input<string | null>(null);
  readonly exportingId = input<number | null>(null);

  readonly view = output<Billing>();
  readonly updateStatus = output<{ id: number; status: PaymentStatus }>();
  readonly exportPdf = output<Billing>();
  readonly viewCloudReport = output<string>();
  readonly delete = output<number>();

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









