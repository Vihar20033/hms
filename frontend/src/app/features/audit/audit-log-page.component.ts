import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { TableModule } from 'primeng/table';
import { HeaderComponent } from '../../layout/header/header.component';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { AuditLog } from './models/audit.models';
import { AuditLogService } from './services/audit-log.service';

@Component({
  selector: 'app-audit-log-page',
  standalone: true,
  imports: [CommonModule, SidebarComponent, HeaderComponent, TableModule],
  template: `
    <div class="dashboard-layout">
      <app-sidebar></app-sidebar>
      <div class="main-content">
        <app-header></app-header>
        <main class="page-body">
          <section class="page-header">
            <div>
              <h1>Audit Trail</h1>
              <p>Review security and operational activity across the hospital system.</p>
            </div>
            <div class="header-actions">
              <input
                #searchBox
                class="search-input"
                type="text"
                [value]="searchQuery"
                placeholder="Search user, action, entity or details"
                (keyup.enter)="applySearch(searchBox.value)"
              />
              <button class="btn-outline" type="button" (click)="applySearch(searchBox.value)">Search</button>
              <button class="btn-primary" type="button" (click)="load()">Refresh</button>
            </div>
          </section>

          @if (errorMessage) {
            <section class="content-card empty-state">
              <h2>Unable to load audit trail</h2>
              <p>{{ errorMessage }}</p>
              <button class="btn-primary" type="button" (click)="load()">Try Again</button>
            </section>
          } @else {
            <section class="content-card">
              <p-table
                [value]="logs"
                [loading]="isLoading"
                responsiveLayout="scroll"
                styleClass="p-datatable-sm audit-table"
              >
                <ng-template pTemplate="header">
                  <tr>
                    <th>Time</th>
                    <th>User</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Details</th>
                  </tr>
                </ng-template>
                <ng-template pTemplate="body" let-log>
                  <tr>
                    <td>{{ log.createdAt | date: 'medium' }}</td>
                    <td>{{ log.username }}</td>
                    <td>{{ log.action.replace('_', ' ') | titlecase }}</td>
                    <td>{{ log.entityType || '-' }} {{ log.entityId || '' }}</td>
                    <td>{{ log.details || '-' }}</td>
                  </tr>
                </ng-template>
                <ng-template pTemplate="emptymessage">
                  <tr>
                    <td colspan="5">
                      <div class="empty-state">No audit activity has been recorded yet.</div>
                    </td>
                  </tr>
                </ng-template>
              </p-table>
              @if (logs.length > 0) {
                <div class="table-footer">
                  <div class="table-footer__meta">Page {{ page + 1 }} • Next {{ hasNext ? 1 : 0 }}</div>
                  <div class="table-footer__actions">
                    <button
                      class="table-footer__btn"
                      type="button"
                      (click)="loadPrevious()"
                      [disabled]="isLoading || isFirstPage"
                    >
                      <i class="ri-arrow-left-s-line"></i>
                      Previous
                    </button>
                    <button
                      class="table-footer__btn table-footer__btn--next"
                      type="button"
                      (click)="loadNext()"
                      [disabled]="isLoading || !hasNext"
                    >
                      Next
                      <i class="ri-arrow-right-s-line"></i>
                    </button>
                  </div>
                </div>
              }
            </section>
          }
        </main>
      </div>
    </div>
  `,
  styles: [
    `
      .page-header,
      .content-card {
        border: 1px solid var(--border-color);
        background: var(--bg-card);
        border-radius: 8px;
        box-shadow: var(--shadow-md);
        padding: 1.25rem;
        margin-bottom: 1rem;
      }
      .page-header {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        align-items: flex-start;
      }
      .header-actions {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        flex-wrap: nowrap;
        justify-content: flex-end;
      }
      .search-input {
        height: 40px;
        min-width: 260px;
        border: 1px solid var(--border-color);
        border-radius: 8px;
        padding: 0 0.75rem;
        background: white;
      }
      .header-actions .btn-outline,
      .header-actions .btn-primary {
        margin-top: 0;
        height: 40px;
        min-height: 40px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        white-space: nowrap;
      }
      .empty-state {
        color: var(--text-muted);
        text-align: center;
        padding: 1.5rem;
      }
      :host ::ng-deep .audit-table .p-datatable-thead > tr > th {
        color: #22384a;
        font-size: 0.8rem;
        font-weight: 800;
        letter-spacing: 0.055em;
        border-bottom: 1px solid rgba(23, 48, 66, 0.22);
        background: #f5f8fb;
      }
      :host ::ng-deep .audit-table .p-datatable-tbody > tr > td {
        border-bottom: 1px solid rgba(23, 48, 66, 0.12);
        color: #1a3143;
        font-weight: 400;
      }
      :host ::ng-deep .audit-table .p-datatable-tbody > tr:hover {
        background: rgba(15, 108, 189, 0.075);
      }
      @media (max-width: 980px) {
        .header-actions {
          flex-wrap: wrap;
          justify-content: flex-start;
        }
      }
    `,
  ],
})
export class AuditLogPageComponent implements OnInit {
  logs: AuditLog[] = [];
  isLoading = true;
  errorMessage = '';
  readonly pageSize = 20;
  searchQuery = '';
  page = 0;
  hasNext = false;

  get isFirstPage(): boolean {
    return this.page === 0;
  }

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.page = 0;
    this.logs = [];
    this.fetchPage();
  }

  applySearch(query: string): void {
    this.searchQuery = query.trim();
    this.load();
  }

  loadNext(): void {
    this.page += 1;
    this.fetchPage();
  }

  loadPrevious(): void {
    if (this.page === 0) return;
    this.page -= 1;
    this.fetchPage();
  }

  private fetchPage(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.auditLogService.getSlice(this.page, this.pageSize, this.searchQuery).subscribe({
      next: (res) => {
        this.logs = res.data.content;
        this.hasNext = res.data.hasNext;
        this.isLoading = false;
      },
      error: (message) => {
        this.errorMessage = message || 'Audit trail is unavailable.';
        this.isLoading = false;
      },
    });
  }
}
