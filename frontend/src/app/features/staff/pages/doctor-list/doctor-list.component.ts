import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { ApiResponse } from '../../../../core/models/common.models';
import { Doctor } from '../../../../core/models/doctor.models';
import { DoctorService } from '../../../../core/services/doctor.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { getDoctorDeleteMessage } from '../../utils/doctor-list.utils';

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
