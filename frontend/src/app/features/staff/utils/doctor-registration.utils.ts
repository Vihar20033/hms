import { formatDepartmentLabel } from '../../../../core/constants/department.constants';
import { Doctor } from '../../../../core/models/doctor.models';

export function buildDoctorDepartmentOptions(departments: string[]): Array<{ label: string; value: string }> {
  return departments.map((department) => ({
    label: formatDepartmentLabel(department),
    value: department,
  }));
}

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
