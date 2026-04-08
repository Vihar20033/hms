import { Billing, PaymentStatus } from '../../models/billing.models';
import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { getBillingStatusClass } from '../../utils/billing-data.utils';

@Component({
  selector: 'app-billing-view-modal',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './billing-view-modal.component.html',
  styleUrl: './billing-view-modal.component.scss'
})
export class BillingViewModalComponent {
  readonly billing = input<Billing | null>(null);
  readonly close = output<void>();
  readonly exportPdf = output<Billing>();

  onClose(): void {
    this.close.emit();
  }

  onExportPdf(): void {
    const billing = this.billing();
    if (billing) {
      this.exportPdf.emit(billing);
    }
  }

  getStatusClass(status: PaymentStatus): string {
    return getBillingStatusClass(status);
  }
}









