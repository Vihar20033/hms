import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { User, Role } from '../../../../core/models/auth.models';
import { UserService } from '../../../../core/services/user.service';
import { ApiResponse } from '../../../../core/models/common.models';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss'
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  isLoading = true;
  Role = Role;

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAll().subscribe({
      next: (res: ApiResponse<User[]>) => {
        this.users = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onDelete(user: User): void {
    if (!user.id) return;

    const confirmed = confirm(`Are you sure you want to delete user ${user.username}? This cannot be undone.`);
    if (confirmed) {
      this.userService.delete(user.id.toString()).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          console.error('Error deleting user', err);
        }
      });
    }
  }

  getRoleBadgeClass(role: Role): string {
    switch (role) {
      case Role.ADMIN: return 'badge-admin';
      case Role.DOCTOR: return 'badge-doctor';
      case Role.NURSE: return 'badge-nurse';
      case Role.PHARMACIST: return 'badge-pharmacy';
      case Role.LABORATORY_STAFF: return 'badge-lab';
      case Role.RECEPTIONIST: return 'badge-recep';
      case Role.PATIENT: return 'badge-patient';
      default: return 'badge-default';
    }
  }
}
