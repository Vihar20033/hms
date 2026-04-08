import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PrinterService {

  print(): void {
    window.print();
  }

  printDocument(title: string): void {
    const originalTitle = document.title;
    document.title = title;
    window.print();
    document.title = originalTitle;
  }
  
  generateMedicalRecordTitle(recordType: string, id: string, patientName: string): string {
    const cleanId = id.split('-')[0].toUpperCase();
    return `${recordType}_${cleanId}_${patientName.replace(/\s+/g, '_')}`;
  }
}






