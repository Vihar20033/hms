import { AuthService } from '../../features/auth/services/auth.service';
import { Billing } from '../../features/billing/models/billing.models';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Doctor } from '../../features/staff/models/doctor.models';
import { LayoutService } from '../services/layout.service';
import { Patient } from '../../features/patients/models/patient.models';
import { Role, User } from '../../features/auth/models/auth.models';
import { ROUTE_ROLES } from '../../core/constants/role-route-map';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

interface SidebarMenuItem {
  title: string;
  icon: string;
  link: string;
  roles?: readonly string[];
  summary?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarComponent {
  private layoutService = inject(LayoutService);
  private authService = inject(AuthService);
  private router = inject(Router);

  get isOpen() { return this.layoutService.isSidebarOpen(); }
  get currentUser() { return this.authService.currentUser; }

  menuItems: SidebarMenuItem[] = [
    {
      title: 'Dashboard',
      icon: 'ri-dashboard-line',
      link: '/dashboard',
      roles: ROUTE_ROLES.dashboard,
      summary: 'Ops command center',
    },
    {
      title: 'User Management',
      icon: 'ri-user-settings-line',
      link: '/users',
      roles: ROUTE_ROLES.users,
      summary: 'Staff identity control',
    },
    {
      title: 'Audit Trail',
      icon: 'ri-shield-check-line',
      link: '/audit-logs',
      roles: ROUTE_ROLES.audit,
      summary: 'Security and activity logs',
    },
    {
      title: 'Patients',
      icon: 'ri-user-heart-line',
      link: '/patients',
      roles: ROUTE_ROLES.patients,
      summary: 'Registration and records',
    },
    {
      title: 'Appointments',
      icon: 'ri-calendar-event-line',
      link: '/appointments',
      roles: ROUTE_ROLES.appointments,
      summary: 'Queue and consultations',
    },

    {
      title: 'Staff',
      icon: 'ri-stethoscope-line',
      link: '/staff',
      roles: ROUTE_ROLES.staff,
      summary: 'Doctor roster and setup',
    },
    {
      title: 'Prescriptions',
      icon: 'ri-file-text-line',
      link: '/prescriptions',
      roles: ROUTE_ROLES.prescriptions,
      summary: 'Clinical orders',
    },
    {
      title: 'Pharmacy',
      icon: 'ri-capsule-line',
      link: '/pharmacy',
      roles: ROUTE_ROLES.pharmacy,
      summary: 'Inventory and stock',
    },
    {
      title: 'Inventory Log',
      icon: 'ri-history-line',
      link: '/pharmacy/inventory-log',
      roles: ROUTE_ROLES.pharmacy,
      summary: 'Stock movement history',
    },
    {
      title: 'Billing',
      icon: 'ri-bill-line',
      link: '/billing',
      roles: ROUTE_ROLES.billing,
      summary: 'Invoices and payments',
    },
  ];

  canView(item: SidebarMenuItem, user: User | null): boolean {
    if (!item.roles) return true;
    if (!user) return false;
    return item.roles.includes(user.role);
  }

  closeSidebar(): void {
    this.layoutService.closeSidebar();
  }

  logout(): void {
    this.closeSidebar();
    this.authService.logout().subscribe({
      complete: () => {
        this.router.navigate(['/login'], { replaceUrl: true });
      },
    });
  }
}






