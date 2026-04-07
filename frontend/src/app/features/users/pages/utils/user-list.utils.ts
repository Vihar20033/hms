import { Role } from '../../../../core/models/auth.models';

export function getRoleBadgeClass(role: Role): string {
  switch (role) {
    case Role.ADMIN:
      return 'badge-admin';
    case Role.DOCTOR:
      return 'badge-doctor';
    case Role.PHARMACIST:
      return 'badge-pharmacy';
    case Role.RECEPTIONIST:
      return 'badge-recep';
    default:
      return 'badge-default';
  }
}
