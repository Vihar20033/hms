import { AuthService } from '../../features/auth/services/auth.service';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { LayoutService } from '../services/layout.service';
import { Role, User } from '../../features/auth/models/auth.models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent {
  private layoutService = inject(LayoutService);
  private authService = inject(AuthService);
  private router = inject(Router);

  get user() { return this.authService.currentUser; }
  today = new Date();

  get roleLabel(): string {
    const role = this.user?.role;
    return role ? role.replaceAll('_', ' ') : 'Staff';
  }

  toggleSidebar(): void {
    this.layoutService.toggleSidebar();
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      complete: () => {
        this.router.navigate(['/login'], { replaceUrl: true });
      },
    });
  }
}






