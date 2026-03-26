export interface PrescriptionMedicine {
  id: number;
  medicineName: string;
  dosage: string;
  duration: string;
  instructions: string;
}

export interface Prescription {
  id: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  appointmentId?: number;
  symptoms: string;
  diagnosis: string;
  medicines: PrescriptionMedicine[];
  advice: string;
  notes: string;
  createdAt: string;
}

export interface PrescriptionRequest {
  patientId: number;
  doctorId: number;
  appointmentId?: number;
  symptoms: string;
  diagnosis: string;
  medicines: PrescriptionMedicineRequest[];
  advice: string;
  notes: string;
}

export interface PrescriptionMedicineRequest {
  medicineName: string;
  dosage: string;
  duration: string;
  instructions: string;
}
