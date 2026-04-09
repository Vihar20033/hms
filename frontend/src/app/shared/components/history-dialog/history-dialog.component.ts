import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { AuditService, EntityRevision } from '../../services/audit.service';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-history-dialog',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    TagModule
  ],
  templateUrl: './history-dialog.component.html',
  styleUrl: './history-dialog.component.scss'
})
export class HistoryDialogComponent implements OnInit {
  revisions: EntityRevision[] = [];
  loading = true;

  constructor(
    public config: DynamicDialogConfig,
    public ref: DynamicDialogRef,
    private auditService: AuditService
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    const { entityType, id } = this.config.data;
    this.auditService.getEntityHistory(entityType, id)
      .pipe(finalize(() => this.loading = false))
      .subscribe({
        next: (res) => this.revisions = res.reverse(),
        error: (err) => console.error('Failed to load history', err)
      });
  }

  getSeverity(type: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast' | undefined {
    switch (type) {
      case 'ADD': return 'success';
      case 'MOD': return 'info';
      case 'DEL': return 'danger';
      default: return 'secondary';
    }
  }

  close(): void {
    this.ref.close();
  }
}
