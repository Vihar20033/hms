import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { User } from '../../../../core/models/auth.models';
import { AuthService } from '../../../../core/services/auth.service';
import { LayoutService } from '../../../../core/services/layout.service';

interface SidebarMenuItem {
  title: string;
  icon: string;
  link: string;
  roles?: string[];
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

  get isOpen() { return this.layoutService.isSidebarOpen(); }
  get currentUser() { return this.authService.currentUser; }

  menuItems: SidebarMenuItem[] = [
    {
      title: 'Dashboard',
      icon: 'ri-dashboard-line',
      link: '/dashboard',
      summary: 'Ops command center',
    },
    {
      title: 'User Management',
      icon: 'ri-user-settings-line',
      link: '/users',
      roles: ['ADMIN'],
      summary: 'Staff identity control',
    },
    {
      title: 'Patients',
      icon: 'ri-user-heart-line',
      link: '/patients',
      roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'],
      summary: 'Registration and records',
    },
    {
      title: 'Appointments',
      icon: 'ri-calendar-event-line',
      link: '/appointments',
      roles: ['ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'],
      summary: 'Queue and consultations',
    },

    {
      title: 'Staff',
      icon: 'ri-stethoscope-line',
      link: '/staff',
      roles: ['ADMIN'],
      summary: 'Doctor roster and setup',
    },
    {
      title: 'Prescriptions',
      icon: 'ri-file-text-line',
      link: '/prescriptions',
      roles: ['ADMIN', 'DOCTOR', 'PHARMACIST'],
      summary: 'Clinical orders',
    },
    {
      title: 'Pharmacy',
      icon: 'ri-capsule-line',
      link: '/pharmacy',
      roles: ['ADMIN', 'PHARMACIST'],
      summary: 'Inventory and stock',
    },
    {
      title: 'Inventory Log',
      icon: 'ri-history-line',
      link: '/pharmacy/inventory-log',
      roles: ['ADMIN', 'PHARMACIST'],
      summary: 'Stock movement history',
    },
    {
      title: 'Billing',
      icon: 'ri-bill-line',
      link: '/billing',
      roles: ['ADMIN', 'RECEPTIONIST'],
      summary: 'Invoices and payments',
    },
    {
      title: 'Logout',
      icon: 'ri-logout-circle-line',
      link: '/login',
      summary: 'Securely exit system',
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
    this.authService.logout();
    window.location.reload();
  }
}
