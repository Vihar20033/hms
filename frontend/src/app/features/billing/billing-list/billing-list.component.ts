import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup } from '@angular/forms';
import { Appointment } from '../../../core/models/appointment.models';
import { Billing, PaymentStatus } from '../../../core/models/billing.models';
import { ApiResponse } from '../../../core/models/common.models';
import { Patient } from '../../../core/models/patient.models';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AuthService } from '../../../core/services/auth.service';
import { BillingService } from '../../../core/services/billing.service';
import { PatientService } from '../../../core/services/patient.service';
import { PdfExportService } from '../../../core/services/pdf-export.service';
import { StatusModalService } from '../../../core/services/status-modal.service';
import { HeaderComponent } from '../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../shared/components/layout/sidebar/sidebar.component';
import { BillingFormComponent } from '../components/billing-form/billing-form.component';
import { BillingTableComponent } from '../components/billing-table/billing-table.component';
import { BillingViewModalComponent } from '../components/billing-view-modal/billing-view-modal.component';
import { createBillingForm, createBillingItemGroup, getBillingItems } from '../utils/billing-form.utils';
import {
  buildBillingPayload,
  buildPreviewItemGroups,
  getBillingSubtotal,
  mapAppointmentOptions,
} from '../utils/billing-data.utils';
import { getBillingStatusClass } from '../utils/billing-data.utils';

@Component({
  selector: 'app-billing-list',
  standalone: true,
  imports: [
    CommonModule,
    HeaderComponent,
    SidebarComponent,
    BillingFormComponent,
    BillingTableComponent,
    BillingViewModalComponent,
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
    private pdfExportService: PdfExportService,
    private cdr: ChangeDetectorRef,
  ) { }

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
    this.billingForm = createBillingForm(this.fb);
    this.billingForm.get('patientId')?.valueChanges.
      subscribe((id: number) => 
        this.onPatientSelectedForManual(id));

    this.billingForm.get('items')?.valueChanges.subscribe(() => {
      const subtotal = this.getSubtotal();
      const tax = subtotal * 0.05;
      this.billingForm.get('taxAmount')?.setValue(tax, { emitEvent: false });
    });
  }

  createItemGroup(): FormGroup {
    return createBillingItemGroup(this.fb);
  }

  get items(): FormArray {
    return getBillingItems(this.billingForm);
  }

  addItem(): void {
    this.items.push(this.createItemGroup());
  }

  removeItem(i: number): void {
    if (this.items.length > 1) this.items.removeAt(i);
  }

  private getSubtotal(): number {
    return getBillingSubtotal(this.items);
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

    this.loadPatientAppointments(patientId, (appointments) => {
      this.patientAppointments = appointments;
    });
  }

  onPatientSelectedForManual(patientId: number): void {
    this.manualPatientAppointments = [];
    this.billingForm.get('appointment')?.setValue(null, { emitEvent: false });

    this.loadPatientAppointments(patientId, (appointments) => {
      this.manualPatientAppointments = appointments;
    });
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
          buildPreviewItemGroups(suggested, (item) => 
            createBillingItemGroup(this.fb, item)).forEach((group) => {
            this.items.push(group);
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
    const taxAmount = this.billingForm.get('taxAmount')?.value || 0;
    const discountAmount = this.billingForm.get('discountAmount')?.value || 0;
    const payload = buildBillingPayload(formValues, this.items, taxAmount, discountAmount);

    this.billingService.create(payload).subscribe({
      next: (response: ApiResponse<Billing>) => {
        this.isSubmitting = false;
        this.closeForm();
        this.loadBillings();
        this.statusModalService.showSuccess(
          'Invoice Created',
          `Invoice ${response.data.invoiceNumber} has been generated.`,
        );
      },
      error: (error: HttpErrorResponse) => {
        this.isSubmitting = false;
        this.statusModalService.showError('Creation Failed', error.error?.message || 'Could not create invoice.');
        this.cdr.markForCheck();
      },
    });
  }

  onExportPdf(billing: Billing): void {
    this.exportingId = billing.id;
    try {
      this.pdfExportService.exportInvoice(billing);
      this.exportingId = null;
    } catch (error) {
      this.statusModalService.showError('Export Failed', 'Could not generate PDF invoice.');
      this.exportingId = null;
    }
  }

  onUpdateStatus(id: number, status: PaymentStatus): void {
    this.billingService.updateStatus(id, status).subscribe({
      next: () => this.loadBillings(),
    });
  }

  getStatusClass(status: PaymentStatus): string {
    return getBillingStatusClass(status);
  }

  onDelete(id: number): void {
    if (!confirm('Delete this billing record?')) return;
    this.billingService.delete(id).subscribe({
      next: () => this.loadBillings(),
    });
  }

  private loadPatientAppointments(
    patientId: number,
    assignAppointments: (appointments: Appointment[]) => void,
  ): void {
    if (!patientId) {
      this.cdr.markForCheck();
      return;
    }

    this.appointmentService.getByPatientId(patientId).subscribe({
      next: (res: ApiResponse<Appointment[]>) => {
        assignAppointments(mapAppointmentOptions(res.data || []));
        this.cdr.markForCheck();
      },
    });
  }
}
