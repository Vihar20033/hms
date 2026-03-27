import { Role, User } from '../../../../core/models/auth.models';

export function filterUsersByTerm(users: User[], term: string): User[] {
  if (!term) {
    return [...users];
  }

  const lowerTerm = term.toLowerCase();
  return users.filter(
    (user) =>
      user.username.toLowerCase().includes(lowerTerm) ||
      user.email.toLowerCase().includes(lowerTerm) ||
      user.role.toLowerCase().includes(lowerTerm),
  );
}

export function getRoleBadgeClass(role: Role): string {
  switch (role) {
    case Role.ADMIN:
      return 'badge-admin';
    case Role.DOCTOR:
      return 'badge-doctor';
    case Role.NURSE:
      return 'badge-nurse';
    case Role.PHARMACIST:
      return 'badge-pharmacy';
    case Role.RECEPTIONIST:
      return 'badge-recep';
    default:
      return 'badge-default';
  }
}
