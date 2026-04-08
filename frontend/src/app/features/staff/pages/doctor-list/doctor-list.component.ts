import { ApiResponse } from '../../../../core/models/common.models';
import { Appointment, Department } from '../../../appointments/models/appointment.models';
import { AppointmentService } from '../../../appointments/services/appointment.service';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { Doctor } from '../../models/doctor.models';
import { DoctorService } from '../../services/doctor.service';
import { DropdownModule } from 'primeng/dropdown';
import { filter, map } from 'rxjs/operators';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { Router, RouterLink } from '@angular/router';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, RouterLink, DialogModule, DropdownModule, FormsModule],
  templateUrl: './doctor-list.component.html',
  styleUrl: './doctor-list.component.scss',
})
export class DoctorListComponent implements OnInit {
  doctors: Doctor[] = [];
  isLoading = true;

  // Pagination
  currentPage = 0;
  pageSize = 12;
  isLastPage = false;
  isMoreLoading = false;

  // Delete Modal State
  deleteModalVisible = false;
  selectedDoctorForDelete: Doctor | null = null;

  // Reassignment Modal State
  reassignModalVisible = false;
  selectedDoctorForReassignment: Doctor | null = null;
  targetDoctorId: number | null = null;
  reassignErrorMessage = '';
  isReassigning = false;
  currentReassignCount = 0;

  constructor(
    private doctorService: DoctorService,
    private appointmentService: AppointmentService,
    private router: Router,
  ) { }

  ngOnInit(): void {
    this.loadDoctors();
  }

  loadDoctors(isLoadMore = false): void {
    if (isLoadMore) {
      this.isMoreLoading = true;
      this.currentPage++;
    } else {
      this.isLoading = true;
      this.currentPage = 0;
      this.doctors = [];
    }

    this.doctorService.getSlice(this.currentPage, this.pageSize).subscribe({
      next: (res) => {
        if (res.data) {
          this.doctors = [...this.doctors, ...res.data.content];
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

  onEdit(doctor: Doctor): void {
    this.router.navigate(['/staff/register'], 
      { queryParams: { doctorId: doctor.id, mode: 'edit' } });
  }

  onDelete(doctor: Doctor): void {
    this.selectedDoctorForDelete = doctor;
    this.deleteModalVisible = true;
  }

  confirmDelete(): void {
    if (!this.selectedDoctorForDelete) return;
    
    // First check appointment count to give admin a heads-up
    this.doctorService.getAppointmentCount(this.selectedDoctorForDelete.id).subscribe({
      next: (res: ApiResponse<number>) => {
        const count = res.data;
        if (count > 0) {
          // Show reassignment modal immediately with count context
          this.deleteModalVisible = false;
          this.selectedDoctorForReassignment = this.selectedDoctorForDelete;
          this.reassignModalVisible = true;
          this.reassignErrorMessage = '';
          this.targetDoctorId = null;
          this.currentReassignCount = count;
        } else {
          // No appointments, proceed with normal delete
          this.performDelete(this.selectedDoctorForDelete!);
        }
      },
      error: () => {
        // Fallback to normal delete attempt if count check fails
        this.performDelete(this.selectedDoctorForDelete!);
      }
    });
  }

  private performDelete(doctor: Doctor): void {
    this.doctorService.delete(doctor.id).subscribe({
      next: () => {
        this.deleteModalVisible = false;
        this.selectedDoctorForDelete = null;
        this.loadDoctors();
      },
      error: (err) => {
        // Handle constraint violation (active appointments)
        if (err.status === 400 || err.status === 409 || err.error?.message?.toLowerCase().includes('appointment')) {
          this.deleteModalVisible = false;
          this.selectedDoctorForReassignment = doctor;
          this.reassignModalVisible = true;
          this.reassignErrorMessage = '';
          this.targetDoctorId = null;
        } else {
          // General error handling
          alert(err.error?.message || 'Failed to delete doctor. Please try again.');
        }
      },
    });
  }

  confirmReassignment(): void {
    if (!this.targetDoctorId || !this.selectedDoctorForReassignment) {
      this.reassignErrorMessage = 'Please select a new doctor for reassignment.';
      return;
    }

    this.isReassigning = true;
    this.appointmentService.reassign(this.selectedDoctorForReassignment.id, this.targetDoctorId).subscribe({
      next: () => {
        // After reassignment, try deleting again
        this.doctorService.delete(this.selectedDoctorForReassignment!.id).subscribe({
          next: () => {
            this.reassignModalVisible = false;
            this.isReassigning = false;
            this.loadDoctors();
          },
          error: (err) => {
            this.reassignErrorMessage = err.error?.message || 'Deletion failed after reassignment.';
            this.isReassigning = false;
          }
        });
      },
      error: (err) => {
        this.reassignErrorMessage = err.error?.message || 'Reassignment failed.';
        this.isReassigning = false;
      }
    });
  }

  get availableDoctorsForReassignment(): any[] {
    return this.doctors
      .filter(d => d.id !== this.selectedDoctorForReassignment?.id)
      .map(d => ({ label: `Dr. ${d.firstName} ${d.lastName} (${d.department})`, value: d.id }));
  }
}












