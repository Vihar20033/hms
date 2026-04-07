import { Role } from '../models/auth.models';

export const ROLE_HOME_ROUTE: Record<Role, string> = {
  [Role.ADMIN]: '/dashboard',
  [Role.DOCTOR]: '/appointments',
  [Role.RECEPTIONIST]: '/appointments',
  [Role.PHARMACIST]: '/pharmacy',
  [Role.LABORATORY_STAFF]: '/lab',
  [Role.PATIENT]: '/patient-portal',
};

export const ROUTE_ROLES = {
  dashboard: [Role.ADMIN, Role.DOCTOR, Role.RECEPTIONIST, Role.PHARMACIST, Role.LABORATORY_STAFF],
  users: [Role.ADMIN],
  audit: [Role.ADMIN],
  lab: [Role.ADMIN, Role.LABORATORY_STAFF],
  patientPortal: [Role.PATIENT],
  patients: [Role.ADMIN, Role.DOCTOR, Role.RECEPTIONIST, Role.PHARMACIST],
  patientRegister: [Role.ADMIN, Role.RECEPTIONIST],
  appointments: [Role.ADMIN, Role.DOCTOR, Role.RECEPTIONIST],
  appointmentBook: [Role.ADMIN, Role.RECEPTIONIST],
  staff: [Role.ADMIN, Role.RECEPTIONIST],
  staffRegister: [Role.ADMIN],
  prescriptions: [Role.ADMIN, Role.DOCTOR, Role.PHARMACIST],
  prescriptionsCreate: [Role.ADMIN, Role.DOCTOR],
  pharmacy: [Role.ADMIN, Role.PHARMACIST],
  billing: [Role.ADMIN, Role.RECEPTIONIST],
} as const;

export function homeRouteForRole(role: Role | string | null | undefined): string {
  return role && role in ROLE_HOME_ROUTE ? ROLE_HOME_ROUTE[role as Role] : '/dashboard';
}

export function canRoleAccessPath(role: Role | string | null | undefined, path: string | null | undefined): boolean {
  if (!role || !path) {
    return false;
  }

  const homeRoute = homeRouteForRole(role);
  if (path === homeRoute || path.startsWith(`${homeRoute}/`)) {
    return true;
  }

  return role !== Role.PATIENT;
}
