import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Billing, PaymentStatus } from '../../../../core/models/billing.models';
import { getBillingStatusClass } from '../../utils/billing-data.utils';

@Component({
  selector: 'app-billing-view-modal',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './billing-view-modal.component.html',
  styleUrl: './billing-view-modal.component.scss'
})
export class BillingViewModalComponent {
  @Input() billing: Billing | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() exportPdf = new EventEmitter<Billing>();

  onClose(): void {
    this.close.emit();
  }

  onExportPdf(): void {
    if (this.billing) {
      this.exportPdf.emit(this.billing);
    }
  }

  getStatusClass(status: PaymentStatus): string {
    return getBillingStatusClass(status);
  }
}
