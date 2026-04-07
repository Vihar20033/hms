import { Appointment } from './appointment.models';
import { Billing } from './billing.models';
import { LabOrder } from './lab.models';
import { Patient } from './patient.models';
import { Prescription } from './prescription.models';

export interface PatientPortalSummary {
  patient: Patient;
  appointments: Appointment[];
  labOrders: LabOrder[];
  prescriptions: Prescription[];
  billings: Billing[];
}
