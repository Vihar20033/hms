import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { finalize } from 'rxjs';
import { AuditService, EntityRevision } from '../../services/audit.service';

@Component({
  selector: 'app-history-dialog',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, TagModule],
  templateUrl: './history-dialog.component.html',
  styleUrl: './history-dialog.component.scss',
})
export class HistoryDialogComponent implements OnInit {
  revisions: EntityRevision[] = [];
  loading = true;
  currentPage = 0;
  pageSize = 10;

  constructor(
    public config: DynamicDialogConfig,
    public ref: DynamicDialogRef,
    private auditService: AuditService,
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  loadHistory(): void {
    const { entityType, id } = this.config.data;
    this.auditService
      .getEntityHistory(entityType, id)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (res) => {
          this.revisions = res.reverse();
          this.currentPage = 0;
        },
        error: (err) => console.error('Failed to load history', err),
      });
  }

  get pagedRevisions(): EntityRevision[] {
    const start = this.currentPage * this.pageSize;
    return this.revisions.slice(start, start + this.pageSize);
  }

  get isFirstPage(): boolean {
    return this.currentPage === 0;
  }

  get hasNextPage(): boolean {
    return (this.currentPage + 1) * this.pageSize < this.revisions.length;
  }

  previousPage(): void {
    if (this.loading || this.isFirstPage) return;
    this.currentPage -= 1;
  }

  nextPage(): void {
    if (this.loading || !this.hasNextPage) return;
    this.currentPage += 1;
  }

  getSeverity(type: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast' | undefined {
    switch (type) {
      case 'ADD':
        return 'success';
      case 'MOD':
        return 'info';
      case 'DEL':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  close(): void {
    this.ref.close();
  }
}
