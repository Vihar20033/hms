export enum Role {
  ADMIN = 'ADMIN',
  DOCTOR = 'DOCTOR',
  NURSE = 'NURSE',
  RECEPTIONIST = 'RECEPTIONIST',
  PHARMACIST = 'PHARMACIST',
  LABORATORY_STAFF = 'LABORATORY_STAFF',
}

export interface User {
  id?: string;
  username: string;
  email: string;
  role: Role;
  enabled?: boolean;
  passwordChangeRequired?: boolean;
}

export interface AuthResponse {
  token: string;
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
