export function canManagePrescriptions(role: string | null): boolean {
  return role === 'ADMIN' || role === 'DOCTOR';
}
