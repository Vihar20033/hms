import { Doctor } from '../../../core/models/doctor.models';

export function getDoctorDeleteMessage(doctor: Doctor): string {
  return `Delete Dr. ${doctor.firstName} ${doctor.lastName}?`;
}
