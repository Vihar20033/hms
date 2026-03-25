import { Injectable } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class LabReportPdfService {
  constructor() {}

  generateLabTestPDF(_testData: any, _reportData: any, _hospitalName: string = 'HMS Hospital'): void {
    console.warn('PDF generation is temporarily disabled');
  }
}
