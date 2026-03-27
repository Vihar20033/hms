import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InputTextModule } from 'primeng/inputtext';
import { TableModule } from 'primeng/table';
import { Role, User } from '../../../../core/models/auth.models';
import { ApiResponse } from '../../../../core/models/common.models';
import { UserService } from '../../../../core/services/user.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { filterUsersByTerm, getRoleBadgeClass } from '../utils/user-list.utils';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, TableModule, InputTextModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  isLoading = true;
  Role = Role;
  searchTerm = '';

  // Event Emitter for search input

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAll().subscribe({
      next: (res: ApiResponse<User[]>) => {
        this.users = res.data;
        this.filterUsers(this.searchTerm);
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onSearchChange(): void {
    this.filterUsers(this.searchTerm);
  }

  filterUsers(term: string): void {
    this.filteredUsers = filterUsersByTerm(this.users, term);
  }

  onDelete(user: User): void {
    if (!user.id) return;

    const confirmed = confirm(`Are you sure you want to delete user ${user.username}? This cannot be undone.`);
    if (confirmed) {
      this.userService.delete(user.id!).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          console.error('Error deleting user', err);
        },
      });
    }
  }

  getRoleBadgeClass(role: Role): string {
    return getRoleBadgeClass(role);
  }
}
