import { FormArray } from '@angular/forms';
import { Appointment } from '../../../core/models/appointment.models';
import { Billing, PaymentMethod, PaymentStatus } from '../../../core/models/billing.models';
import { Patient } from '../../../core/models/patient.models';
import { DateUtils } from '../../../core/utils/date.utils';

export function getBillingItemTotal(items: FormArray, index: number): number {
  const item = items.at(index).value;
  return (item.quantity || 0) * (item.unitPrice || 0);
}

export function getBillingSubtotal(items: FormArray): number {
  return items.controls.reduce((sum, _, index) => sum + getBillingItemTotal(items, index), 0);
}

export function getBillingNetTotal(items: FormArray, taxAmount: number, discountAmount: number): number {
  return getBillingSubtotal(items) + (taxAmount || 0) - (discountAmount || 0);
}

export function mapAppointmentOptions(appointments: Appointment[]): Appointment[] {
  return appointments
    .filter((appointment) => appointment.status === 'COMPLETED' || appointment.status === 'CHECKED_IN')
    .map((appointment) => ({
      ...appointment,
      label: `${new Date(appointment.appointmentTime).toLocaleDateString()} - ${appointment.department}`,
    }));
}

export function buildBillingPayload(formValue: any, items: FormArray, taxAmount: number, discountAmount: number): any {
  const subtotal = getBillingSubtotal(items);

  return {
    ...formValue,
    appointmentId: formValue.appointment?.id || null,
    totalAmount: subtotal,
    taxAmount,
    netAmount: getBillingNetTotal(items, taxAmount, discountAmount),
    paymentStatus: 'UNPAID',
    billingDate: DateUtils.formatLocalDateTime(new Date()),
    dueDate: DateUtils.formatDate(formValue.dueDate),
    items: items.controls.map((control) => {
      const value = control.value;
      return {
        ...value,
        totalValue: (value.quantity || 0) * (value.unitPrice || 0),
      };
    }),
  };
}

export function getBillingStatusClass(status: PaymentStatus): string {
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

export function buildPatientOptions(patients: Patient[]): Array<{ label: string; value: number }> {
  return patients.map((patient) => ({ label: patient.name, value: patient.id }));
}

export function buildPaymentMethodOptions(
  paymentMethods: PaymentMethod[],
): Array<{ label: PaymentMethod; value: PaymentMethod }> {
  return paymentMethods.map((method) => ({ label: method, value: method }));
}

export function buildPreviewItemGroups(
  suggested: Billing,
  createGroup: (item: Partial<{ itemName: string; quantity: number; unitPrice: number }>) => any,
): any[] {
  return (suggested.items || []).map((item) =>
    createGroup({
      itemName: item.itemName,
      quantity: item.quantity,
      unitPrice: item.unitPrice,
    }),
  );
}
