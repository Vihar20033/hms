import { ApiResponse } from '../../../../core/models/common.models';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { getRoleBadgeClass } from '../utils/user-list.utils';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { Role, User } from '../../../auth/models/auth.models';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { TableModule } from 'primeng/table';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, TableModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  isLoading = true;
  Role = Role;

  constructor(
    private userService: UserService,
    private statusModalService: StatusModalService,
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
      },
    });
  }

  async onDelete(user: User): Promise<void> {
    if (!user.id) return;

    const confirmed = await this.statusModalService.confirm(
      'Delete User',
      `Delete user ${user.username}? This cannot be undone.`,
      'Delete',
    );
    if (confirmed) {
      this.userService.delete(user.id!).subscribe({
        next: () => {
          this.statusModalService.showSuccess('User Deleted', 'The user account was removed.');
          this.loadUsers();
        },
        error: (err) => {
          this.statusModalService.showError('Delete Failed', err.error?.message || 'Could not delete this user.');
        },
      });
    }
  }

  getRoleBadgeClass(role: Role): string {
    return getRoleBadgeClass(role);
  }
}













