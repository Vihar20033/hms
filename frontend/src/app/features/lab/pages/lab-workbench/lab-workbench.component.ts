import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LabOrder, LabOrderStatus } from '../../../../core/models/lab.models';
import { LabService } from '../../../../core/services/lab.service';
import { HeaderComponent } from '../../../../shared/components/layout/header/header.component';
import { SidebarComponent } from '../../../../shared/components/layout/sidebar/sidebar.component';

@Component({
  selector: 'app-lab-workbench',
  standalone: true,
  imports: [CommonModule, RouterLink, SidebarComponent, HeaderComponent],
  templateUrl: './lab-workbench.component.html',
  styleUrl: './lab-workbench.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LabWorkbenchComponent implements OnInit {
  queue: LabOrder[] = [];
  isLoading = true;

  readonly workflow = [
    'Verify patient identity and test order.',
    'Collect and label specimen at source.',
    'Run analyzer workflow and validate result.',
    'Release result to doctor review queue.',
  ];

  constructor(private labService: LabService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.isLoading = true;
    this.labService.getAll().subscribe({
      next: (res) => {
        this.queue = res.data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  get urgentCount(): number {
    return this.queue.filter((item) => item.status === LabOrderStatus.ORDERED).length;
  }

  get readyCount(): number {
    return this.queue.filter((item) => item.status === LabOrderStatus.COMPLETED).length;
  }
}
