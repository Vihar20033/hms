import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiResponse } from '../../../../core/models/common.models';
import { Appointment } from '../../../appointments/models/appointment.models';
import { AppointmentService } from '../../../appointments/services/appointment.service';
import { AuthService } from '../../../auth/services/auth.service';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Doctor } from '../../../staff/models/doctor.models';
import { DoctorService } from '../../../staff/services/doctor.service';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { from } from 'rxjs';
import { HeaderComponent } from '../../../../layout/header/header.component';
import { HttpErrorResponse } from '@angular/common/http';
import { Medicine } from '../../../pharmacy/models/pharmacy.models';
import { PharmacyService } from '../../../pharmacy/services/pharmacy.service';
import { Prescription, PrescriptionRequest } from '../../models/prescription.models';
import { PrescriptionService } from '../../services/prescription.service';
import { Role, User } from '../../../auth/models/auth.models';
import { SidebarComponent } from '../../../../layout/sidebar/sidebar.component';
import { AutoCompleteModule } from 'primeng/autocomplete';
import {
  calculatePrescriptionQuantity,
  createPrescriptionForm,
  createPrescriptionMedicineGroup,
  getPrescriptionMedicines,
  stockValidator,
} from '../../utils/prescription-create-form';
import { filterPrescriptionMedicines, findDoctorIdByUserEmail } from '../../utils/prescription-create.utils';

@Component({
  selector: 'app-prescription-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SidebarComponent, HeaderComponent, RouterLink, AutoCompleteModule],
  templateUrl: './prescription-create.component.html',
  styleUrl: './prescription-create.component.scss',
})
export class PrescriptionCreateComponent implements OnInit {
  prescriptionForm!: FormGroup;
  appointmentId: number | null = null;
  patientId: number | null = null;
  doctorId: number | null = null;
  patientName = '';
  appointment?: Appointment;
  isSubmitting = false;
  errorMessage = '';
  availableMedicines: Medicine[] = [];
  filteredMedicines: Medicine[] = [];

  constructor(
    private fb: FormBuilder,
    private prescriptionService: PrescriptionService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private doctorService: DoctorService,
    private pharmacyService: PharmacyService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const paramId = this.route.snapshot.params['appointmentId'];
    this.appointmentId = paramId ? Number(paramId) : null;
    if (this.appointmentId) {
      this.loadAppointmentDetails();
    }
    this.loadMedicines();
    this.initForm();
  }

  private loadMedicines(): void {
    this.pharmacyService.getActive().subscribe((res: ApiResponse<Medicine[]>) => {
      this.availableMedicines = res.data;
      this.filteredMedicines = [...this.availableMedicines];
      this.cdr.markForCheck();
    });
  }

  filterMedicines(event: { query: string }): void {
    this.filteredMedicines = filterPrescriptionMedicines(this.availableMedicines, event.query);
  }

  onMedicineSelect(event: { value: Medicine }, index: number): void {
    const medicine = event.value as Medicine;
    const medicineFormGroup = this.medicines.at(index) as FormGroup;

    // Auto-fill medicine name if it's an object (PrimeNG returns the object if selected from list)
    if (typeof medicine === 'object') {
      medicineFormGroup.patchValue({
        medicineName: medicine.name,
        medicineId: medicine.id,
        availableStock: medicine.quantityInStock,
      });
      // Trigger quantity validation immediately
      medicineFormGroup.get('quantity')?.updateValueAndValidity();
    }
  }

  private initForm(): void {
    this.prescriptionForm = createPrescriptionForm(this.fb);
    // Wire up valueChanges for the initial medicine group (Medication #1)
    const initialGroup = this.medicines.at(0) as FormGroup;
    initialGroup.get('dosage')?.valueChanges.subscribe(() => this.calculateQuantity(initialGroup));
    initialGroup.get('duration')?.valueChanges.subscribe(() => this.calculateQuantity(initialGroup));
  }

  get medicines(): FormArray {
    return getPrescriptionMedicines(this.prescriptionForm);
  }

  createMedicineGroup(): FormGroup {
    const group = createPrescriptionMedicineGroup(this.fb);
    group.get('dosage')?.valueChanges.subscribe(() => this.calculateQuantity(group));
    group.get('duration')?.valueChanges.subscribe(() => this.calculateQuantity(group));

    return group;
  }

  private calculateQuantity(group: FormGroup): void {
    calculatePrescriptionQuantity(group);
  }

  addMedicine(): void {
    this.medicines.push(this.createMedicineGroup());
  }

  removeMedicine(index: number): void {
    if (this.medicines.length > 1) {
      this.medicines.removeAt(index);
    }
  }

  private loadAppointmentDetails(): void {
    this.appointmentService.getById(this.appointmentId!).subscribe
    ((res: ApiResponse<Appointment>) => {
      const a = res.data;
      if (a) {
        this.appointment = a;
        this.patientId = a.patientId || null;
        this.patientName = a.patientName || '';

        if (a.doctorId) {
          this.doctorId = a.doctorId;
          this.cdr.markForCheck();
        } else {
          const user = this.authService.currentUser;
          if (user?.role === 'DOCTOR') {
            this.doctorService.getAll().subscribe((doctorsRes: ApiResponse<Doctor[]>) => {
              this.doctorId = findDoctorIdByUserEmail(doctorsRes.data, user.email);
              this.cdr.markForCheck();
            });
          }
        }
      }
    });
  }

  onSubmit(completeConsultation = false): void {
    if (this.prescriptionForm.invalid || !this.patientId || !this.doctorId) return;

    this.isSubmitting = true;
    const request: PrescriptionRequest = {
      patientId: this.patientId!,
      doctorId: this.doctorId!,
      appointmentId: this.appointmentId!,
      ...this.prescriptionForm.value,
    };

    this.prescriptionService.create(request).subscribe({
      next: () => {
        if (completeConsultation && this.appointmentId) {
          this.appointmentService.completeConsultation(this.appointmentId).subscribe({
            next: () => this.router.navigate(['/appointments']),
            error: () => this.router.navigate(['/appointments'])
          });
        } else {
          this.router.navigate(['/appointments']);
        }
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || err.message || 'Failed to save prescription. Check inventory.';
      },
    });
  }
}












