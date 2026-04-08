import { Department } from '../../appointments/models/appointment.models';
import { Doctor } from '../models/doctor.models';
import { formatDepartmentLabel } from '../../../core/constants/department.constants';
import { map } from 'rxjs/operators';

// Build dropdown options for doctor department selection
export function buildDoctorDepartmentOptions(departments: string[]): Array<{ label: string; value: string }> {
  return departments.map((department) => ({
    label: formatDepartmentLabel(department),
    value: department,
  }));
}

// Build a patch object for doctor update by comparing form values with existing doctor data
export function buildDoctorFormPatch(doctor: Doctor): Record<string, unknown> {
  return {
    firstName: doctor.firstName,
    lastName: doctor.lastName,
    specialization: doctor.specialization,
    department: doctor.department,
    email: doctor.email,
    phoneNumber: doctor.phoneNumber || doctor.contactNumber || '',
    consultationFee: doctor.consultationFee,
    licenseNumber: doctor.licenseNumber || doctor.registrationNumber,
    qualification: doctor.qualification || '',
    experienceYears: doctor.experienceYears ?? 0,
    designation: doctor.designation || '',
    bio: doctor.bio || '',
    username: '',
    temporaryPassword: '********',
  };
}







