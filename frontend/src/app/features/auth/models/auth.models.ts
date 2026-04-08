import { Doctor } from '../../staff/models/doctor.models';
import { Patient } from '../../patients/models/patient.models';

export enum Role {
  ADMIN = 'ADMIN',
  DOCTOR = 'DOCTOR',
  RECEPTIONIST = 'RECEPTIONIST',
  PHARMACIST = 'PHARMACIST'
}

export interface User {
  id?: number;
  username: string;
  email: string;
  role: Role;
  enabled?: boolean;
  passwordChangeRequired?: boolean;
}

export interface AuthResponse {
  token?: string;
  username: string;
  email: string;
  role: Role;
  passwordChangeRequired: boolean;
}

export interface LoginRequest {
  username: string;
  password?: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password?: string;
  role: Role;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
