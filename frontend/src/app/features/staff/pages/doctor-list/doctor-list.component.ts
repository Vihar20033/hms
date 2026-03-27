import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ApiResponse } from '../../../../core/models/common.models';
import { Doctor } from '../../../../core/models/doctor.models';
import { DoctorService } from '../../../../core/services/doctor.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { getDoctorDeleteMessage } from '../../utils/doctor-list.utils';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink],
  templateUrl: './doctor-list.component.html',
  styleUrl: './doctor-list.component.scss',
})
export class DoctorListComponent implements OnInit {
  doctors: Doctor[] = [];
  isLoading = true;

  constructor(
    private doctorService: DoctorService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.loadDoctors();
  }

  loadDoctors(): void {
    this.isLoading = true;
    this.doctorService.getAll().subscribe({
      next: (res: ApiResponse<Doctor[]>) => {
        this.doctors = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onEdit(doctor: Doctor): void {
    this.router.navigate(['/staff/register'], { queryParams: { doctorId: doctor.id, mode: 'edit' } });
  }

  onDelete(doctor: Doctor): void {
    const confirmed = confirm(getDoctorDeleteMessage(doctor));
    if (!confirmed) {
      return;
    }

    this.doctorService.delete(doctor.id).subscribe({
      next: () => {
        this.loadDoctors();
      },
      error: () => {
        // Error logging handled by interceptor or console
      },
    });
  }
}
