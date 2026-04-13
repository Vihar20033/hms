import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReindexStatus } from '../../models/elasticsearch-admin.models';
import { ElasticsearchAdminService } from '../../services/elasticsearch-admin.service';

@Component({
  selector: 'app-elasticsearch-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './elasticsearch-admin.component.html',
  styleUrls: ['./elasticsearch-admin.component.scss'],
})
export class ElasticsearchAdminComponent implements OnInit {
  isLoading = false;
  elasticsearchHealth: boolean | null = null;
  lastReindexStatus: ReindexStatus | null = null;
  reindexingInProgress = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  reindexOptions = [
    { label: 'All Entities', value: 'all' },
    { label: 'Patients Only', value: 'patients' },
    { label: 'Doctors Only', value: 'doctors' },
    { label: 'Appointments Only', value: 'appointments' },
    { label: 'Prescriptions Only', value: 'prescriptions' },
  ];

  selectedReindexOption = 'all';

  constructor(private elasticsearchAdminService: ElasticsearchAdminService) {}

  ngOnInit() {
    this.checkHealth();
  }

  /**
   * Check Elasticsearch health status
   */
  checkHealth() {
    this.isLoading = true;
    this.elasticsearchAdminService.checkHealth().subscribe({
      next: (response) => {
        this.elasticsearchHealth = response.data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Health check failed:', error);
        this.elasticsearchHealth = false;
        this.errorMessage = 'Failed to check Elasticsearch health';
        this.isLoading = false;
      },
    });
  }

  /**
   * Perform full reindex
   */
  performReindex() {
    if (this.reindexingInProgress) {
      return;
    }

    if (!confirm('Are you sure you want to reindex? This may take some time.')) {
      return;
    }

    this.reindexingInProgress = true;
    this.errorMessage = null;
    this.successMessage = null;

    let reindexObservable;

    switch (this.selectedReindexOption) {
      case 'patients':
        reindexObservable = this.elasticsearchAdminService.reindexPatients();
        break;
      case 'doctors':
        reindexObservable = this.elasticsearchAdminService.reindexDoctors();
        break;
      case 'appointments':
        reindexObservable = this.elasticsearchAdminService.reindexAppointments();
        break;
      case 'prescriptions':
        reindexObservable = this.elasticsearchAdminService.reindexPrescriptions();
        break;
      case 'all':
      default:
        reindexObservable = this.elasticsearchAdminService.reindexAll();
        break;
    }

    reindexObservable.subscribe({
      next: (response) => {
        this.reindexingInProgress = false;
        this.lastReindexStatus = response.data;
        this.successMessage = response.message || 'Reindex completed successfully!';
        console.log('Reindex completed:', response.data);
      },
      error: (error) => {
        this.reindexingInProgress = false;
        console.error('Reindex failed:', error);
        this.errorMessage = error.error?.message || 'Reindex failed. Please try again.';
      },
    });
  }

  /**
   * Clear all indices
   */
  clearAllIndices() {
    if (!confirm('WARNING: This will permanently delete all search indices. Are you absolutely sure?')) {
      return;
    }

    if (!confirm('This action cannot be undone. Confirm again?')) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.elasticsearchAdminService.clearAllIndices().subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = response.message || 'All indices cleared successfully!';
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Failed to clear indices';
      },
    });
  }

  /**
   * Close alert messages
   */
  closeAlert() {
    this.successMessage = null;
    this.errorMessage = null;
  }

  /**
   * Calculate duration between two dates
   */
  calculateDuration(startTime: string, endTime: string): string {
    try {
      const start = new Date(startTime).getTime();
      const end = new Date(endTime).getTime();
      const durationMs = end - start;
      const durationSecs = Math.round(durationMs / 1000);

      if (durationSecs < 60) {
        return `${durationSecs} seconds`;
      } else if (durationSecs < 3600) {
        const mins = Math.round(durationSecs / 60);
        return `${mins} minute(s)`;
      } else {
        const hours = Math.round(durationSecs / 3600);
        return `${hours} hour(s)`;
      }
    } catch (e) {
      return 'Unknown';
    }
  }
}
