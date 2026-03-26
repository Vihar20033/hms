import { CommonModule, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './header.component.html',
  styleUrl: './header.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeaderComponent {
  
  get user() { return this.authService.currentUser; }
  today = new Date();

  get roleLabel(): string {
    const role = this.user?.role;
    return role ? role.replaceAll('_', ' ') : 'Staff';
  }

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
