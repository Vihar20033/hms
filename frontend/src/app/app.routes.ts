import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { ROUTE_ROLES } from './core/constants/role-route-map';

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
      roles: ROUTE_ROLES.dashboard,
    },
  },
  {
    path: 'users',
    loadComponent: () => import('./features/users/pages/user-list/user-list.component').then(m => m.UserListComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ROUTE_ROLES.users },
  },
  {
    path: 'audit-logs',
    loadComponent: () => import('./features/audit/audit-log-page.component').then(m => m.AuditLogPageComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ROUTE_ROLES.audit },
  },
  {
    path: 'lab',
    loadComponent: () => import('./features/lab/pages/lab-workbench/lab-workbench.component')
      .then(m => m.LabWorkbenchComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ROUTE_ROLES.lab },
  },
  {
    path: 'patient-portal',
    loadComponent: () => import('./features/patient-portal/pages/patient-home/patient-home.component')
      .then(m => m.PatientHomeComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ROUTE_ROLES.patientPortal },
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
          roles: ROUTE_ROLES.patients,
        },
      },
      {
        path: 'register',
        loadComponent: () => import('./features/patients/pages/patient-registration/patient-registration.component').then(m => m.PatientRegistrationComponent),
        data: { roles: ROUTE_ROLES.patientRegister },
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
          roles: ROUTE_ROLES.appointments,
        },
      },
      {
        path: 'book',
        loadComponent: () => import('./features/appointments/pages/appointment-booking/appointment-booking.component').then(m => m.AppointmentBookingComponent),
        data: { roles: ROUTE_ROLES.appointmentBook },
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
        data: { roles: ROUTE_ROLES.staff },
      },
      {
        path: 'register',
        loadComponent: () => import('./features/staff/pages/doctor-registration/doctor-registration.component').then(m => m.DoctorRegistrationComponent),
        data: { roles: ROUTE_ROLES.staffRegister },
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
        data: { roles: ROUTE_ROLES.prescriptions },
      },
      {
        path: 'create/:appointmentId',
        loadComponent: () => import('./features/prescription/pages/prescription-create/prescription-create.component').then(m => m.PrescriptionCreateComponent),
        data: { roles: ROUTE_ROLES.prescriptionsCreate },
      },
      {
        path: ':id',
        loadComponent: () => import('./features/prescription/pages/prescription-detail/prescription-detail.component').then(m => m.PrescriptionDetailComponent),
        data: { roles: ROUTE_ROLES.prescriptions },
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
        data: { roles: ROUTE_ROLES.pharmacy },
      },
      {
        path: 'inventory-log',
        loadComponent: () => import('./features/pharmacy/pages/inventory-log/inventory-log.component').then(m => m.InventoryLogComponent),
        data: { roles: ROUTE_ROLES.pharmacy },
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
        loadComponent: () => import('./features/billing/billing-list/billing-list.component').then(m => m.BillingListComponent),
        data: { roles: ROUTE_ROLES.billing },
      },
    ],
  },

  { path: '**', redirectTo: 'login' },
];

