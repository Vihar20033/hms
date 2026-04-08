import { AuditLog } from './models/audit.models';
import { AuditLogService } from './services/audit-log.service';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HeaderComponent } from '../../layout/header/header.component';
import { SidebarComponent } from '../../layout/sidebar/sidebar.component';
import { TableModule } from 'primeng/table';
import { User } from '../auth/models/auth.models';

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
            <button class="btn-primary" type="button" (click)="load()">Refresh</button>
          </section>

          @if (errorMessage) {
            <section class="content-card empty-state">
              <h2>Unable to load audit trail</h2>
              <p>{{ errorMessage }}</p>
              <button class="btn-primary" type="button" (click)="load()">Try Again</button>
            </section>
          } @else {
            <section class="content-card">
              <p-table [value]="logs" [loading]="isLoading" responsiveLayout="scroll" styleClass="p-datatable-sm">
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
              @if (hasNext) {
                <button class="btn-outline" type="button" (click)="loadNext()">Load More</button>
              }
            </section>
          }
        </main>
      </div>
    </div>
  `,
  styles: [`
    .page-header, .content-card {
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
    .empty-state {
      color: var(--text-muted);
      text-align: center;
      padding: 1.5rem;
    }
    .btn-outline {
      margin-top: 1rem;
      min-height: 40px;
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 0 1rem;
      background: white;
      font-weight: 700;
    }
  `],
})
export class AuditLogPageComponent implements OnInit {
  logs: AuditLog[] = [];
  isLoading = true;
  errorMessage = '';
  page = 0;
  hasNext = false;

  constructor(private auditLogService: AuditLogService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.page = 0;
    this.logs = [];
    this.fetchPage();
  }

  loadNext(): void {
    this.page += 1;
    this.fetchPage(true);
  }

  private fetchPage(append = false): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.auditLogService.getSlice(this.page, 25).subscribe({
      next: (res) => {
        this.logs = append ? [...this.logs, ...res.data.content] : res.data.content;
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









