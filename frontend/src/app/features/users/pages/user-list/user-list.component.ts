import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { User, Role } from '../../../../core/models/auth.models';
import { UserService } from '../../../../core/services/user.service';
import { ApiResponse } from '../../../../core/models/common.models';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { FormsModule } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, TableModule, InputTextModule, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss'
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  isLoading = true;
  Role = Role;
  searchTerm = '';
  private searchSubject = new Subject<string>();

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(term => {
      this.filterUsers(term);
    });
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAll().subscribe({
      next: (res: ApiResponse<User[]>) => {
        this.users = res.data;
        this.filterUsers('');
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchTerm);
  }

  filterUsers(term: string): void {
    if (!term) {
      this.filteredUsers = [...this.users];
      return;
    }
    const lowerTerm = term.toLowerCase();
    this.filteredUsers = this.users.filter(u => 
      u.username.toLowerCase().includes(lowerTerm) || 
      u.email.toLowerCase().includes(lowerTerm) ||
      u.role.toLowerCase().includes(lowerTerm)
    );
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
      default: return 'badge-default';
    }
  }
}
