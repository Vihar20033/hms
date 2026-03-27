import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TableModule } from 'primeng/table';
import { Appointment } from '../../../../core/models/appointment.models';
import { Billing, PaymentMethod, PaymentStatus } from '../../../../core/models/billing.models';
import { ApiResponse, PagedResponse } from '../../../../core/models/common.models';
import { Patient } from '../../../../core/models/patient.models';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { PatientService } from '../../../../core/services/patient.service';
import { StatusModalService } from '../../../../core/services/status-modal.service';
import { futureOrTodayDateValidator, trimRequired } from '../../../../core/validators/app-validators';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-billing-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    SidebarComponent,
    HeaderComponent,
    DropdownModule,
    CalendarModule,
    InputNumberModule,
    InputTextModule,
    InputTextareaModule,
    TableModule,
    DatePipe,
    DecimalPipe
  ],
  templateUrl: './billing-list.component.html',
  styleUrl: './billing-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BillingListComponent implements OnInit {
  billings: Billing[] = [];
  patients: Patient[] = [];
  isLoading = true;
  showCreateForm = false;
  isSubmitting = false;
  exportingId: number | null = null;
  selectedBilling: Billing | null = null;
  showViewModal = false;
  
  billingForm!: FormGroup;
  today: Date = new Date();

  paymentMethods = Object.values(PaymentMethod);
  PaymentStatus = PaymentStatus;

  showAutoGenerateModal = false;
  selectedPatientIdForAuto: number | null = null;
  patientAppointments: Appointment[] = [];
  selectedAppointmentId: number | null = null;

  manualPatientAppointments: Appointment[] = [];
  isSyncingItems = false;
  isGenerating = false;

  userRole: string | null = null;

  constructor(
    private billingService: BillingService,
    private patientService: PatientService,
    private appointmentService: AppointmentService,
    private authService: AuthService,
    private fb: FormBuilder,
    private statusModalService: StatusModalService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.getUserRole();
    this.initForm();
    this.loadBillings();
    this.patientService.getAll().subscribe((res: ApiResponse<Patient[]>) => {
      this.patients = res.data;
      this.cdr.markForCheck();
    });
  }

  initForm(): void {
    this.billingForm = this.fb.group({
      patientId: ['', Validators.required],
      appointment: [null],
      paymentMethod: ['CASH'],
      taxAmount: [0, Validators.min(0)],
      discountAmount: [0, Validators.min(0)],
      notes: ['', Validators.maxLength(1000)],
      dueDate: [null, [futureOrTodayDateValidator()]],
      insuranceProvider: [''],
      insuranceClaimNumber: [''],
      insuranceAmount: [0, Validators.min(0)],
      insuranceStatus: ['PENDING'],
      items: this.fb.array([this.createItemGroup()]),
    });

    this.billingForm.get('patientId')?.valueChanges.subscribe((id) => this.onPatientSelectedForManual(id));

    this.billingForm.get('items')?.valueChanges.subscribe(() => {
      const subtotal = this.getSubtotal();
      const tax = subtotal * 0.05;
      this.billingForm.get('taxAmount')?.setValue(tax, { emitEvent: false });
    });
  }

  createItemGroup(): FormGroup {
    return this.fb.group({
      itemName: ['', [...trimRequired(2, 200)]],
      quantity: [1, [Validators.required, Validators.min(1)]],
      unitPrice: [null, [Validators.required, Validators.min(0.01)]],
    });
  }

  get items(): FormArray {
    return this.billingForm.get('items') as FormArray;
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
  }

  removeItem(i: number): void {
    if (this.items.length > 1) this.items.removeAt(i);
  }

  getItemTotal(i: number): number {
    const item = this.items.at(i).value;
    return (item.quantity || 0) * (item.unitPrice || 0);
  }

  getSubtotal(): number {
    return this.items.controls.reduce((sum, _, i) => sum + this.getItemTotal(i), 0);
  }

  getNetTotal(): number {
    const sub = this.getSubtotal();
    const tax = this.billingForm.get('taxAmount')?.value || 0;
    const discount = this.billingForm.get('discountAmount')?.value || 0;
    return sub + tax - discount;
  }

  loadBillings(): void {
    this.isLoading = true;
    this.billingService.getAll().subscribe({
      next: (res: ApiResponse<Billing[]>) => {
        this.billings = res.data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (error) => {
        console.error('Error loading billings:', error);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  openCreateForm(): void {
    this.initForm();
    this.manualPatientAppointments = [];
    this.showCreateForm = true;
  }

  closeForm(): void {
    this.showCreateForm = false;
  }

  onView(bill: Billing): void {
    this.selectedBilling = bill;
    this.showViewModal = true;
  }

  closeViewModal(): void {
    this.showViewModal = false;
    this.selectedBilling = null;
  }

  openAutoGenerateModal(): void {
    this.showAutoGenerateModal = true;
    this.selectedPatientIdForAuto = null;
    this.patientAppointments = [];
    this.selectedAppointmentId = null;
  }

  onPatientSelectedForAuto(patientId: number): void {
    this.selectedPatientIdForAuto = patientId;
    this.patientAppointments = [];
    this.selectedAppointmentId = null;

    if (patientId) {
      this.appointmentService.getByPatientId(patientId).subscribe({
        next: (res: ApiResponse<PagedResponse<Appointment>>) => {
          this.patientAppointments = (res.data.content || [])
            .filter((a: Appointment) => a.status === 'COMPLETED' || a.status === 'CHECKED_IN')
            .map((a: Appointment) => ({ ...a, label: `${new Date(a.appointmentTime).toLocaleDateString()} - ${a.department}` }));
          this.cdr.markForCheck();
        },
      });
    }
  }

  onPatientSelectedForManual(patientId: number): void {
    this.manualPatientAppointments = [];
    this.billingForm.get('appointment')?.setValue(null, { emitEvent: false });

    if (patientId) {
      this.appointmentService.getByPatientId(patientId).subscribe({
        next: (res: ApiResponse<PagedResponse<Appointment>>) => {
          this.manualPatientAppointments = (res.data.content || [])
            .filter((a: Appointment) => a.status === 'COMPLETED' || a.status === 'CHECKED_IN')
            .map((a: Appointment) => ({ ...a, label: `${new Date(a.appointmentTime).toLocaleDateString()} - ${a.department}` }));
          this.cdr.markForCheck();
        },
      });
    }
  }

  onAppointmentSelectedForManual(appointment: any): void {
    if (!appointment || !appointment.id) return;

    this.isSyncingItems = true;
    this.billingService.getPreviewFromAppointment(appointment.id).subscribe({
      next: (res) => {
        const suggested = res.data;
        this.isSyncingItems = false;

        if (suggested.items && suggested.items.length > 0) {
          while (this.items.length) {
            this.items.removeAt(0);
          }
          suggested.items.forEach((item) => {
            this.items.push(
              this.fb.group({
                itemName: [item.itemName, [...trimRequired(2, 200)]],
                quantity: [item.quantity, [Validators.required, Validators.min(1)]],
                unitPrice: [item.unitPrice, [Validators.required, Validators.min(0.01)]],
              }),
            );
          });
        }
        this.cdr.markForCheck();
      },
      error: () => {
        this.isSyncingItems = false;
        this.statusModalService.showError('Sync Failed', 'Could not load items from this appointment.');
        this.cdr.markForCheck();
      },
    });
  }

  generateFromAppointment(): void {
    if (!this.selectedAppointmentId) return;

    this.isGenerating = true;
    this.billingService.generateFromAppointment(this.selectedAppointmentId).subscribe({
      next: () => {
        this.isGenerating = false;
        this.showAutoGenerateModal = false;
        this.statusModalService.showSuccess(
          'Bill Generated',
          'Invoice auto-calculated from consultation and medicines.',
        );
        this.loadBillings();
      },
      error: (err: HttpErrorResponse) => {
        this.isGenerating = false;
        const msg = err.error?.message || err.message || 'Could not auto-generate bill for this appointment.';
        this.statusModalService.showError('Generation Failed', msg);
        this.cdr.markForCheck();
      },
    });
  }

  onSubmit(): void {
    if (this.billingForm.invalid) {
      this.billingForm.markAllAsTouched();
      return;
    }
    
    this.isSubmitting = true;
    const formValues = this.billingForm.value;
    const payload = {
      ...formValues,
      appointmentId: formValues.appointment?.id || null,
      totalAmount: this.getSubtotal(),
      taxAmount: this.billingForm.get('taxAmount')?.value || 0,
      netAmount: this.getNetTotal(),
      paymentStatus: 'UNPAID',
      billingDate: this.formatLocalDateTime(new Date()),
      dueDate: this.formatDate(this.billingForm.get('dueDate')?.value),
      items: this.items.controls.map((control) => {
        const value = control.value;
        return {
          ...value,
          totalValue: (value.quantity || 0) * (value.unitPrice || 0),
        };
      }),
    };

    this.billingService.create(payload).subscribe({
      next: (response: ApiResponse<Billing>) => {
        this.isSubmitting = false;
        this.closeForm();
        this.loadBillings();
        this.statusModalService.showSuccess('Invoice Created', `Invoice ${response.data.invoiceNumber} has been generated.`);
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.statusModalService.showError('Creation Failed', error.error?.message || 'Could not create invoice.');
        this.cdr.markForCheck();
      },
    });
  }

  onUpdateStatus(id: number, status: PaymentStatus): void {
    this.billingService.updateStatus(id, status).subscribe({
      next: () => this.loadBillings(),
    });
  }

  onExportPdf(billing: Billing): void {
    this.exportingId = billing.id;
    try {
      const doc = new jsPDF();
      doc.text('Artemis Hospital Invoice', 20, 20);
      doc.text(`Invoce #: ${billing.invoiceNumber}`, 20, 30);
      doc.text(`Patient: ${billing.patientName}`, 20, 40);
      doc.text(`Total: INR ${billing.netAmount}`, 20, 50);
      doc.save(`invoice-${billing.invoiceNumber}.pdf`);
      this.exportingId = null;
    } catch (error) {
      console.error('PDF Generation Error:', error);
      this.exportingId = null;
    }
  }

  getStatusClass(status: PaymentStatus): string {
    const map: Record<string, string> = {
      PAID: 'status-completed',
      UNPAID: 'status-scheduled',
      PENDING: 'status-scheduled',
      PARTIAL: 'status-checked-in',
      OVERDUE: 'status-cancelled',
      CANCELLED: 'status-cancelled',
      REFUNDED: 'status-checked-in',
    };
    return map[status] || 'status-scheduled';
  }

  onDelete(id: number): void {
    if (!confirm('Delete this billing record?')) return;
    this.billingService.delete(id).subscribe({
      next: () => this.loadBillings(),
    });
  }

  getPatientOptions() {
    return this.patients.map(p => ({ label: p.name, value: p.id }));
  }

  getPaymentMethodOptions() {
    return this.paymentMethods.map(m => ({ label: m, value: m }));
  }

  private formatDate(value: Date | null): string | null {
    if (!value) return null;
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private formatLocalDateTime(value: Date): string {
    const year = value.getFullYear();
    const month = String(value.getMonth() + 1).padStart(2, '0');
    const day = String(value.getDate()).padStart(2, '0');
    const hours = String(value.getHours()).padStart(2, '0');
    const mins = String(value.getMinutes()).padStart(2, '0');
    const secs = String(value.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${mins}:${secs}`;
  }
}
