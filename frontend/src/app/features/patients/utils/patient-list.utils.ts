export function canRegisterPatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'RECEPTIONIST' || role === 'NURSE';
}

export function canEditPatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'DOCTOR' || role === 'RECEPTIONIST' || role === 'NURSE';
}

export function canDeletePatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'RECEPTIONIST';
}
