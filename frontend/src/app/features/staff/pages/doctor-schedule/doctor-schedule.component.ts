import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DAYS_OF_WEEK, DAY_LABELS, DoctorSchedule } from '../../../../core/models/doctor-schedule.models';
import { Doctor } from '../../../../core/models/doctor.models';
import { DoctorScheduleService } from '../../../../core/services/doctor-schedule.service';
import { DoctorService } from '../../../../core/services/doctor.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-doctor-schedule',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterLink, 
    SidebarComponent, 
    HeaderComponent,
    DropdownModule,
    CalendarModule,
    ButtonModule,
    TooltipModule
  ],
  templateUrl: './doctor-schedule.component.html',
  styleUrl: './doctor-schedule.component.scss',
})
export class DoctorScheduleComponent implements OnInit {
  doctor: Doctor | null = null;
  schedules: DoctorSchedule[] = [];
  scheduleForm: FormGroup;

  days = DAYS_OF_WEEK;
  dayLabels = DAY_LABELS;

  isLoading = true;
  isSaving = false;
  doctorId: string | null = null;

  get dayOptions() {
    return this.days.map(d => ({ label: this.dayLabels[d], value: d }));
  }

  get durationOptions() {
    return [
      { label: '15 Minutes', value: 15 },
      { label: '20 Minutes', value: 20 },
      { label: '30 Minutes', value: 30 },
      { label: '45 Minutes', value: 45 },
      { label: '60 Minutes', value: 60 }
    ];
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private doctorService: DoctorService,
    private scheduleService: DoctorScheduleService,
  ) {
    this.scheduleForm = this.fb.group({
      dayOfWeek: ['', Validators.required],
      startTime: [new Date().setHours(9, 0, 0, 0), Validators.required],
      endTime: [new Date().setHours(17, 0, 0, 0), Validators.required],
      slotDurationMinutes: [30, [Validators.required, Validators.min(5)]],
    });
  }

  ngOnInit(): void {
    this.doctorId = this.route.snapshot.paramMap.get('id');
    if (!this.doctorId) {
      this.router.navigate(['/staff']);
      return;
    }

    this.loadData();
  }

  loadData(): void {
    if (!this.doctorId) return;
    this.isLoading = true;

    // Load doctor details
    this.doctorService.getById(this.doctorId).subscribe({
      next: (res) => {
        this.doctor = res.data;
        this.loadSchedules();
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  loadSchedules(): void {
    if (!this.doctorId) return;
    this.scheduleService.getByDoctor(this.doctorId).subscribe({
      next: (res) => {
        this.schedules = res.data;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
      },
    });
  }

  onSubmit(): void {
    if (this.scheduleForm.invalid || !this.doctorId) return;

    this.isSaving = true;
    const formValue = this.scheduleForm.value;
    
    // Format dates back to HH:mm for backend
    const request = {
      ...formValue,
      doctorId: this.doctorId,
      startTime: this.formatTimeForBackend(formValue.startTime),
      endTime: this.formatTimeForBackend(formValue.endTime)
    };

    this.scheduleService.create(request).subscribe({
      next: () => {
        this.isSaving = false;
        this.loadSchedules();
        this.scheduleForm.patchValue({ dayOfWeek: '' });
      },
      error: () => {
        this.isSaving = false;
      },
    });
  }

  private formatTimeForBackend(date: any): string {
    if (!date) return '00:00';
    const d = new Date(date);
    const h = d.getHours().toString().padStart(2, '0');
    const m = d.getMinutes().toString().padStart(2, '0');
    return `${h}:${m}`;
  }

  onDelete(scheduleId: string): void {
    if (!confirm('Are you sure you want to delete this availability slot?')) return;

    this.scheduleService.delete(scheduleId).subscribe({
      next: () => {
        this.loadSchedules();
      },
      error: () => {
        // Error logging handled by interceptor or console
      },
    });
  }

  formatTime(timeStr: string): string {
    if (!timeStr) return '';
    // Backend gives "HH:mm:ss", we want "h:mm A"
    const [h, m] = timeStr.split(':');
    const hour = parseInt(h);
    const suffix = hour >= 12 ? 'PM' : 'AM';
    const hour12 = hour % 12 || 12;
    return `${hour12}:${m} ${suffix}`;
  }
}
