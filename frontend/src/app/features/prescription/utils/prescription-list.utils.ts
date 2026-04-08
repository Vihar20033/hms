import { Doctor } from '../../staff/models/doctor.models';
import { Role } from '../../auth/models/auth.models';

export function canManagePrescriptions(role: string | null): boolean {
  return role === 'ADMIN' || role === 'DOCTOR';
}





