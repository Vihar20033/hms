import { Doctor } from '../../staff/models/doctor.models';
import { filter } from 'rxjs/operators';
import { Medicine } from '../../pharmacy/models/pharmacy.models';

// filter medicines based on query matching name or code
export function filterPrescriptionMedicines(medicines: Medicine[], query: string): Medicine[] {
  const normalizedQuery = query.toLowerCase();
  return medicines.filter(
    (medicine) =>
      medicine.name.toLowerCase().includes(normalizedQuery) ||
      medicine.medicineCode?.toLowerCase().includes(normalizedQuery),
  );
}

export function findDoctorIdByUserEmail(doctors: Doctor[], email?: string | null): number | null {
  if (!email) {
    return null;
  }

  const doctor = doctors.find((item) => item.email === email);
  return doctor?.id ?? null;
}







