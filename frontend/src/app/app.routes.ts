import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { 
    path: 'login', 
    loadComponent: () => import('./features/auth/pages/login/login.component')
    .then(m => m.LoginComponent),
    canActivate: [guestGuard] 
  },
  { 
    path: 'register', 
    loadComponent: () => import('./features/auth/pages/register/register.component')
    .then(m => m.RegisterComponent),
    canActivate: [guestGuard] 
  },
  {
    path: 'change-password',
    loadComponent: () => import('./features/auth/pages/change-password/change-password.component')
    .then(m => m.ChangePasswordComponent),
    canActivate: [authGuard],
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./features/auth/pages/unauthorized/unauthorized.component')
    .then(m => m.UnauthorizedComponent),
    canActivate: [authGuard],
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/pages/dashboard.component')
    .then(m => m.DashboardComponent),
    canActivate: [authGuard, roleGuard],
    data: {
      roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST'],
    },
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/pages/user-list/user-list.component').then(m => m.UserListComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
  },

  // Patient Routes
  {
    path: 'patients',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/patients/pages/patient-list/patient-list.component').then(m => m.PatientListComponent),
        data: {
          roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PHARMACIST'],
        },
      },
      {
        path: 'register',
        loadComponent: () => import('./features/patients/pages/patient-registration/patient-registration.component').then(m => m.PatientRegistrationComponent),
        data: { roles: ['ADMIN', 'RECEPTIONIST'] },
      },
    ],
  },

  // Appointment Routes
  {
    path: 'appointments',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/appointments/pages/appointment-list/appointment-list.component').then(m => m.AppointmentListComponent),
        data: {
          roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'],
        },
      },
      {
        path: 'book',
        loadComponent: () => import('./features/appointments/pages/appointment-booking/appointment-booking.component').then(m => m.AppointmentBookingComponent),
        data: { roles: ['ADMIN', 'RECEPTIONIST'] },
      },
    ],
  },

  // Staff Routes
  {
    path: 'staff',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/staff/pages/doctor-list/doctor-list.component').then(m => m.DoctorListComponent),
        data: { roles: ['ADMIN', 'RECEPTIONIST'] },
      },
      {
        path: 'register',
        loadComponent: () => import('./features/staff/pages/doctor-registration/doctor-registration.component').then(m => m.DoctorRegistrationComponent),
        data: { roles: ['ADMIN'] },
      },
    ],
  },

  // Clinical Routes
  {
    path: 'clinical',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: 'vitals-list',
        loadComponent: () => import('./features/clinical/pages/vitals-list/vitals-list.component').then(m => m.VitalsListComponent),
        data: { roles: ['ADMIN', 'NURSE'] },
      },
      {
        path: 'vitals/:appointmentId',
        loadComponent: () => import('./features/clinical/pages/vitals-record/vitals-record.component').then(m => m.VitalsRecordComponent),
        data: { roles: ['ADMIN', 'NURSE'] },
      },
    ],
  },

  // Prescription Routes
  {
    path: 'prescriptions',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/prescription/pages/prescription-list/prescription-list.component').then(m => m.PrescriptionListComponent),
        data: { roles: ['ADMIN', 'DOCTOR', 'PHARMACIST'] },
      },
      {
        path: 'create/:appointmentId',
        loadComponent: () => import('./features/prescription/pages/prescription-create/prescription-create.component').then(m => m.PrescriptionCreateComponent),
        data: { roles: ['ADMIN', 'DOCTOR'] },
      },
      {
        path: ':id',
        loadComponent: () => import('./features/prescription/pages/prescription-detail/prescription-detail.component').then(m => m.PrescriptionDetailComponent),
        data: { roles: ['ADMIN', 'DOCTOR', 'PHARMACIST'] },
      },
    ],
  },

  // Pharmacy Routes
  {
    path: 'pharmacy',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/pharmacy/pages/pharmacy-list/pharmacy-list.component').then(m => m.PharmacyListComponent),
        data: { roles: ['ADMIN', 'PHARMACIST'] },
      },
      {
        path: 'inventory-log',
        loadComponent: () => import('./features/pharmacy/pages/inventory-log/inventory-log.component').then(m => m.InventoryLogComponent),
        data: { roles: ['ADMIN', 'PHARMACIST'] },
      },
    ],
  },

  // Billing Routes
  {
    path: 'billing',
    canActivate: [authGuard],
    canActivateChild: [authGuard, roleGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/billing/pages/billing-list/billing-list.component').then(m => m.BillingListComponent),
        data: { roles: ['ADMIN', 'RECEPTIONIST'] },
      },
    ],
  },

  { path: '**', redirectTo: 'login' },
];

