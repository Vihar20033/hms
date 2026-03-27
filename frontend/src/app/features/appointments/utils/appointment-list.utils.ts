import { Appointment, AppointmentStatus } from '../../../core/models/appointment.models';

export function getAppointmentPageLead(userRole: string | null): string {
  if (userRole === 'DOCTOR') {
    return 'Track only the patients assigned to you and start each consultation from here.';
  }

  if (userRole === 'RECEPTIONIST') {
    return 'Manage arrivals, move patients into the doctor queue, and keep the OPD flowing.';
  }

  return 'View and manage all medical consultations.';
}

export function getAppointmentStatusClass(status: string): string {
  return `status-${status.toLowerCase()}`;
}

export function getAppointmentWorkflowLabel(appointment: Appointment, userRole: string | null): string {
  switch (appointment.status) {
    case AppointmentStatus.SCHEDULED:
      return userRole === 'DOCTOR' ? 'Waiting for reception check-in' : 'Scheduled and awaiting arrival';
    case AppointmentStatus.CHECKED_IN:
      return userRole === 'DOCTOR' ? 'Ready to start consultation' : 'Vitals and doctor handoff pending';
    case AppointmentStatus.IN_CONSULTATION:
      return 'Consultation in progress';
    case AppointmentStatus.COMPLETED:
      return 'Consultation complete';
    case AppointmentStatus.CANCELLED:
      return 'Visit cancelled';
    default:
      return 'Active appointment';
  }
}

export function canManageAppointmentForRole(userRole: string | null): boolean {
  return userRole === 'ADMIN' || userRole === 'RECEPTIONIST';
}

export function canDoctorStartAppointment(appointment: Appointment, userRole: string | null): boolean {
  return appointment.status === AppointmentStatus.CHECKED_IN && (userRole === 'DOCTOR' || userRole === 'ADMIN');
}
