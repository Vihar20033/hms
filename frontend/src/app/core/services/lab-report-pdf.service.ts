import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LabReportPdfService {
  constructor() {}

  /**
   * Generate and download PDF for lab test report
   */
  generateLabTestPDF(_testData: any, _reportData: any, _hospitalName: string = 'HMS Hospital'): void {
    console.warn('PDF generation is temporarily disabled');
  }
}
