import { CommonModule, DatePipe } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { FormArray, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { Appointment } from '../../../../core/models/appointment.models';
import { PaymentMethod } from '../../../../core/models/billing.models';
import { Patient } from '../../../../core/models/patient.models';
import { buildPatientOptions, buildPaymentMethodOptions, getBillingItemTotal, getBillingNetTotal, getBillingSubtotal } from '../../utils/billing-data.utils';

@Component({
  selector: 'app-billing-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DropdownModule,
    CalendarModule,
    InputNumberModule,
    InputTextModule,
    InputTextareaModule,
    DatePipe
  ],
  templateUrl: './billing-form.component.html',
  styleUrl: './billing-form.component.scss'
})
export class BillingFormComponent {
  readonly form = input.required<FormGroup>();
  readonly patients = input<Patient[]>([]);
  readonly manualPatientAppointments = input<Appointment[]>([]);
  readonly isSyncingItems = input(false);
  readonly isSubmitting = input(false);
  readonly today = input(new Date());

  readonly close = output<void>();
  readonly submitForm = output<void>();
  readonly addItem = output<void>();
  readonly removeItem = output<number>();
  readonly appointmentSelected = output<any>();

  PaymentMethod = PaymentMethod;
  paymentMethods = Object.values(PaymentMethod) as PaymentMethod[];

  get items(): FormArray {
    return this.form().get('items') as FormArray;
  }

  onClose(): void {
    this.close.emit();
  }

  onSubmit(): void {
    this.submitForm.emit();
  }

  onAddItem(): void {
    this.addItem.emit();
  }

  onRemoveItem(index: number): void {
    this.removeItem.emit(index);
  }

  onAppointmentSelected(appointment: any): void {
    this.appointmentSelected.emit(appointment);
  }

  getItemTotal(i: number): number {
    return getBillingItemTotal(this.items, i);
  }

  getSubtotal(): number {
    return getBillingSubtotal(this.items);
  }

  getNetTotal(): number {
    const tax = this.form().get('taxAmount')?.value || 0;
    const discount = this.form().get('discountAmount')?.value || 0;
    return getBillingNetTotal(this.items, tax, discount);
  }

  getPatientOptions() {
    return buildPatientOptions(this.patients());
  }

  getPaymentMethodOptions() {
    return buildPaymentMethodOptions(this.paymentMethods);
  }
}
