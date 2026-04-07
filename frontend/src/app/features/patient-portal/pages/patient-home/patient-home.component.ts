import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { User } from '../../../../core/models/auth.models';
import { PaymentStatus } from '../../../../core/models/billing.models';
import { PatientPortalSummary } from '../../../../core/models/patient-portal.models';
import { AuthService } from '../../../../core/services/auth.service';
import { BillingService } from '../../../../core/services/billing.service';
import { PatientPortalService } from '../../../../core/services/patient-portal.service';
import { StatusModalService } from '../../../../core/services/status-modal.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-patient-home',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent],
  templateUrl: './patient-home.component.html',
  styleUrl: './patient-home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PatientHomeComponent implements OnInit {
  summary: PatientPortalSummary | null = null;
  isLoading = true;
  errorMessage = '';
  paymentStatus = PaymentStatus;

  readonly careSteps = [
    'Check upcoming appointment details with the reception team.',
    'Keep prescriptions and billing references ready for follow-up visits.',
    'Use the patient desk for demographic changes and medical record updates.',
  ];

  constructor(
    private authService: AuthService,
    private patientPortalService: PatientPortalService,
    private billingService: BillingService,
    private statusModalService: StatusModalService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.patientPortalService.getSummary().subscribe({
      next: (res) => {
        this.summary = res.data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (message) => {
        this.errorMessage = message || 'Unable to load your patient portal right now.';
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  get user(): User | null {
    return this.authService.currentUser;
  }

  get unpaidBillCount(): number {
    return this.summary?.billings?.filter((bill) => bill.paymentStatus !== PaymentStatus.PAID).length || 0;
  }

  payBill(id: number): void {
    this.billingService.payMine(id).subscribe({
      next: () => {
        this.statusModalService.showSuccess('Payment recorded', 'Your bill has been marked as paid.');
        this.reloadSummary();
      },
      error: (message) => this.statusModalService.showError('Payment failed', message),
    });
  }

  private reloadSummary(): void {
    this.patientPortalService.getSummary().subscribe({
      next: (res) => {
        this.summary = res.data;
        this.cdr.markForCheck();
      },
    });
  }
}
