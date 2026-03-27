import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AutoCompleteModule } from 'primeng/autocomplete';
import { Appointment } from '../../../../core/models/appointment.models';
import { ApiResponse } from '../../../../core/models/common.models';
import { Doctor } from '../../../../core/models/doctor.models';
import { Medicine } from '../../../../core/models/pharmacy.models';
import { PrescriptionRequest } from '../../../../core/models/prescription.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { DoctorService } from '../../../../core/services/doctor.service';
import { PharmacyService } from '../../../../core/services/pharmacy.service';
import { PrescriptionService } from '../../../../core/services/prescription.service';
import { trimRequired } from '../../../../core/validators/app-validators';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

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
    const query = event.query.toLowerCase();
    this.filteredMedicines = this.availableMedicines.filter(
      (m) => m.name.toLowerCase().includes(query) || m.medicineCode?.toLowerCase().includes(query),
    );
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
    this.prescriptionForm = this.fb.group({
      symptoms: ['', Validators.maxLength(1000)],
      diagnosis: ['', [...trimRequired(2, 1000)]],
      medicines: this.fb.array([this.createMedicineGroup()]),
      advice: ['', Validators.maxLength(1000)],
      notes: ['', Validators.maxLength(2000)],
    });
  }

  get medicines(): FormArray {
    return this.prescriptionForm.get('medicines') as FormArray;
  }

  createMedicineGroup(): FormGroup {
    const group = this.fb.group(
      {
        medicineId: [''],
        availableStock: [0],
        medicineName: ['', [...trimRequired(2, 200)]],
        dosage: ['', [...trimRequired(1, 200)]],
        duration: ['', [...trimRequired(1, 100)]],
        quantity: [1, [Validators.required, Validators.min(1)]],
        instructions: ['', Validators.maxLength(500)],
      },
      { validators: this.stockValidator },
    );

    // Auto-calculate quantity when dosage or duration changes
    group.get('dosage')?.valueChanges.subscribe(() => this.calculateQuantity(group));
    group.get('duration')?.valueChanges.subscribe(() => this.calculateQuantity(group));

    return group;
  }

  private stockValidator(group: FormGroup): ValidationErrors | null {
    const qty = group.get('quantity')?.value;
    const stock = group.get('availableStock')?.value;
    const medName = group.get('medicineName')?.value;

    if (medName && qty > stock && stock !== null) {
      return { insufficientStock: { actual: stock, requested: qty } };
    }
    return null;
  }

  private calculateQuantity(group: FormGroup): void {
    const dosage = group.get('dosage')?.value || '';
    const duration = group.get('duration')?.value || '';

    if (!dosage || !duration) return;

    // 1. Calculate doses per day
    // Pattern: 1-1-1 or 1-0-1 or 1,1,1
    const dosesArray = dosage.match(/\d+/g);
    const dosesPerDay = dosesArray ? dosesArray.reduce((acc: number, val: string) => acc + parseInt(val), 0) : 0;

    // 2. Calculate duration in days
    // Pattern: 5 days or 5 or 1 week
    const durationMatch = duration.match(/\d+/);
    let durationInDays = durationMatch ? parseInt(durationMatch[0]) : 0;

    if (duration.toLowerCase().includes('week')) {
      durationInDays *= 7;
    } else if (duration.toLowerCase().includes('month')) {
      durationInDays *= 30;
    }

    if (dosesPerDay > 0 && durationInDays > 0) {
      group.patchValue(
        {
          quantity: dosesPerDay * durationInDays,
        },
        { emitEvent: false },
      );
    }
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
        this.appointmentService.getById(this.appointmentId!).subscribe((res: ApiResponse<Appointment>) => {
      const appt = res.data;
      if (appt) {
        this.appointment = appt;
        this.patientId = appt.patientId || null;
        this.patientName = appt.patientName || '';

        if (appt.doctorId) {
          this.doctorId = appt.doctorId;
          this.cdr.markForCheck();
        } else {
          const user = this.authService.currentUser;
          if (user?.role === 'DOCTOR') {
            this.doctorService.getAll().subscribe((doctorsRes: ApiResponse<Doctor[]>) => {
              const currentDoctor = doctorsRes.data.find((d) => d.email === user.email);
              if (currentDoctor) {
                this.doctorId = currentDoctor.id;
              }
              this.cdr.markForCheck();
            });
          }
        }
      }
    });
  }

  onSubmit(): void {
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
        this.router.navigate(['/appointments']);
      },
      error: (err: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || err.message || 'Failed to save prescription. Check inventory.';
      },
    });
  }
}
