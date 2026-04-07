export function canRegisterPatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'RECEPTIONIST';
}

export function canEditPatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'DOCTOR' || role === 'RECEPTIONIST';
}

export function canDeletePatient(role: string | null): boolean {
  return role === 'ADMIN' || role === 'RECEPTIONIST';
}
