import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../../shared/services/status-modal.service';
import { Role, User } from '../../../auth/models/auth.models';
import { UserService } from '../../services/user.service';
import { getRoleBadgeClass } from '../utils/user-list.utils';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, TableModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
})
export class UserListComponent implements OnInit, OnDestroy {
  users: User[] = [];
  isLoading = true;
  Role = Role;
  searchQuery = '';
  searchSubject = new Subject<string>();
  currentPage = 0;
  pageSize = 20;
  isLastPage = false;
  isMoreLoading = false;

  constructor(
    private userService: UserService,
    private statusModalService: StatusModalService,
  ) {}

  ngOnInit(): void {
    this.searchSubject.pipe(debounceTime(350), distinctUntilChanged()).subscribe((query) => {
      this.searchQuery = query;
      this.loadUsers();
    });
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.searchSubject.complete();
  }

  loadUsers(isLoadMore = false): void {
    if (isLoadMore) {
      this.isMoreLoading = true;
      this.currentPage++;
    } else {
      this.isLoading = true;
      this.currentPage = 0;
      this.users = [];
    }

    this.userService.getSlice(this.currentPage, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        if (res.data) {
          this.users = isLoadMore ? [...this.users, ...res.data.content] : res.data.content;
          this.isLastPage = res.data.last;
        }
        this.isLoading = false;
        this.isMoreLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.isMoreLoading = false;
      },
    });
  }

  onSearch(event: Event): void {
    this.searchSubject.next((event.target as HTMLInputElement).value);
  }

  clearSearch(): void {
    if (!this.searchQuery) {
      return;
    }

    this.searchQuery = '';
    this.searchSubject.next('');
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
