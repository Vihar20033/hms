import { autoTable } from 'jspdf-autotable';
import { Billing, PaymentStatus } from '../../features/billing/models/billing.models';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { jsPDF } from 'jspdf';
import { map } from 'rxjs/operators';
import { Patient } from '../../features/patients/models/patient.models';

@Injectable({
  providedIn: 'root'
})
export class PdfExportService {

  constructor(
    private datePipe: DatePipe,
    private decimalPipe: DecimalPipe
  ) {}

  exportInvoice(billing: Billing): void {
    if (billing.reportUrl) {
      window.open(billing.reportUrl, '_blank');
      return;
    }

    const doc = new jsPDF();
    const primaryColor: [number, number, number] = [26, 115, 232]; // Artemis Blue

    // Header
    doc.setFontSize(22);
    doc.setTextColor(primaryColor[0], primaryColor[1], primaryColor[2]);
    doc.text('ARTEMIS HEALTHCARE', 105, 25, { align: 'center' });
    
    doc.setFontSize(10);
    doc.setTextColor(100);
    doc.text('Advanced Medical Care & Research Center', 105, 32, { align: 'center' });
    doc.line(20, 38, 190, 38);

    // Invoice Info
    doc.setFontSize(12);
    doc.setTextColor(0);
    doc.setFont('helvetica', 'bold');
    doc.text('INVOICE', 20, 50);
    
    doc.setFont('helvetica', 'normal');
    doc.setFontSize(10);
    doc.text(`Invoice No: ${billing.invoiceNumber}`, 20, 58);
    doc.text(`Date: ${this.datePipe.transform(billing.billingDate, 'mediumDate')}`, 20, 64);
    doc.text(`Due Date: ${this.datePipe.transform(billing.dueDate, 'mediumDate') || 'N/A'}`, 20, 70);
    doc.text(`Status: ${billing.paymentStatus}`, 20, 76);

    // Patient Info
    doc.setFont('helvetica', 'bold');
    doc.text('BILL TO:', 120, 50);
    doc.setFont('helvetica', 'normal');
    doc.text(`Patient: ${billing.patientName}`, 120, 58);
    if (billing.insuranceProvider) {
      doc.text(`Insurance: ${billing.insuranceProvider}`, 120, 64);
      doc.text(`Claim: ${billing.insuranceClaimNumber}`, 120, 70);
    }

    // Items Table
    const tableData = billing.items.map((item, index) => [
      index + 1,
      item.itemName,
      item.quantity,
      `INR ${this.decimalPipe.transform(item.unitPrice, '1.2-2')}`,
      `INR ${this.decimalPipe.transform(item.totalValue, '1.2-2')}`
    ]);

    autoTable(doc, {
      startY: 85,
      head: [['#', 'Description', 'Qty', 'Unit Price', 'Total']],
      body: tableData,
      theme: 'grid',
      headStyles: { fillColor: primaryColor, textColor: 255 },
      styles: { fontSize: 9 },
      columnStyles: {
        0: { cellWidth: 10 },
        2: { cellWidth: 20, halign: 'center' },
        3: { cellWidth: 35, halign: 'right' },
        4: { cellWidth: 35, halign: 'right' }
      }
    });

    const finalY = (doc as any).lastAutoTable.finalY + 10;

    // Summary
    doc.setFontSize(10);
    const summaryX = 140;
    doc.text('Subtotal:', summaryX, finalY);
    doc.text(`INR ${this.decimalPipe.transform(billing.totalAmount, '1.2-2')}`, 190, finalY, { align: 'right' });
    
    doc.text('Tax (5%):', summaryX, finalY + 7);
    doc.text(`+ INR ${this.decimalPipe.transform(billing.taxAmount, '1.2-2')}`, 190, finalY + 7, { align: 'right' });
    
    doc.text('Discount:', summaryX, finalY + 14);
    doc.text(`- INR ${this.decimalPipe.transform(billing.discountAmount || 0, '1.2-2')}`, 190, finalY + 14, { align: 'right' });

    doc.setFont('helvetica', 'bold');
    doc.line(135, finalY + 18, 190, finalY + 18);
    doc.text('NET AMOUNT:', summaryX, finalY + 24);
    doc.text(`INR ${this.decimalPipe.transform(billing.netAmount, '1.2-2')}`, 190, finalY + 24, { align: 'right' });

    // Footer
    doc.setFontSize(8);
    doc.setFont('helvetica', 'italic');
    doc.setTextColor(150);
    doc.text('Thank you for choosing Artemis Healthcare. Get well soon!', 105, 280, { align: 'center' });

    doc.save(`Invoice_${billing.invoiceNumber}.pdf`);
  }
}








