import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../../layout/header/header.component';
import { SidebarComponent } from '../../../layout/sidebar/sidebar.component';
import { StatusModalService } from '../../../shared/services/status-modal.service';
import {
  CreateWorkflowDefinitionRequest,
  StartWorkflowInstanceRequest,
  TransitionWorkflowRequest,
  WorkflowDefinitionResponse,
  WorkflowDomain,
  WorkflowInstanceResponse,
  WorkflowTaskResponse,
} from '../models/workflow-admin.models';
import { WorkflowAdminService } from '../services/workflow-admin.service';

@Component({
  selector: 'app-workflow-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, SidebarComponent, HeaderComponent],
  templateUrl: './workflow-admin.component.html',
  styleUrl: './workflow-admin.component.scss',
})
export class WorkflowAdminComponent {
  readonly domainOptions: WorkflowDomain[] = ['OPD', 'IPD', 'EMERGENCY', 'SURGERY', 'DISCHARGE', 'REFERRAL'];

  loadingDefinitions = false;
  loadingInstance = false;
  transitionLoading = false;

  definitionFilter = '';
  definitions: WorkflowDefinitionResponse[] = [];

  definitionForm: CreateWorkflowDefinitionRequest = {
    definitionKey: 'OPD_FLOW',
    name: 'OPD Standard Flow',
    domain: 'OPD',
    initialStepCode: 'REGISTRATION',
    description: 'Default OPD clinical workflow',
    steps: [
      { stepCode: 'REGISTRATION', name: 'Patient Registration', stepOrder: 1, assigneeRole: 'RECEPTIONIST' },
      { stepCode: 'TRIAGE', name: 'Clinical Triage', stepOrder: 2, assigneeRole: 'DOCTOR' },
      { stepCode: 'CONSULTATION', name: 'Doctor Consultation', stepOrder: 3, assigneeRole: 'DOCTOR' },
      { stepCode: 'DISPENSE', name: 'Prescription Dispense', stepOrder: 4, assigneeRole: 'PHARMACIST' },
      { stepCode: 'COMPLETE', name: 'Workflow Complete', stepOrder: 5, terminalStep: true },
    ],
    transitions: [
      { fromStepCode: 'REGISTRATION', toStepCode: 'TRIAGE', actionLabel: 'START_TRIAGE' },
      { fromStepCode: 'TRIAGE', toStepCode: 'CONSULTATION', actionLabel: 'BEGIN_CONSULTATION' },
      { fromStepCode: 'CONSULTATION', toStepCode: 'DISPENSE', actionLabel: 'ISSUE_PRESCRIPTION' },
      { fromStepCode: 'DISPENSE', toStepCode: 'COMPLETE', actionLabel: 'CLOSE_VISIT' },
    ],
  };

  startForm: StartWorkflowInstanceRequest = {
    definitionKey: 'OPD_FLOW',
    referenceType: 'APPOINTMENT',
    referenceId: '',
    contextJson: '',
  };

  transitionForm: TransitionWorkflowRequest = {
    actionLabel: '',
    approvalGranted: false,
    notes: '',
    assigneeUserId: null,
  };

  selectedInstanceId = '';
  currentInstance: WorkflowInstanceResponse | null = null;
  currentTasks: WorkflowTaskResponse[] = [];

  constructor(
    private workflowAdminService: WorkflowAdminService,
    private statusModalService: StatusModalService,
  ) {
    this.loadDefinitions();
  }

  addStep(): void {
    const nextOrder = this.definitionForm.steps.length + 1;
    this.definitionForm.steps = [
      ...this.definitionForm.steps,
      {
        stepCode: '',
        name: '',
        stepOrder: nextOrder,
      },
    ];
  }

  removeStep(index: number): void {
    if (this.definitionForm.steps.length <= 1) {
      this.statusModalService.showWarning('Invalid Operation', 'A workflow requires at least one step.');
      return;
    }

    this.definitionForm.steps = this.definitionForm.steps.filter((_, i) => i !== index);
  }

  addTransition(): void {
    this.definitionForm.transitions = [
      ...this.definitionForm.transitions,
      {
        fromStepCode: '',
        toStepCode: '',
        actionLabel: '',
      },
    ];
  }

  removeTransition(index: number): void {
    if (this.definitionForm.transitions.length <= 1) {
      this.statusModalService.showWarning('Invalid Operation', 'A workflow requires at least one transition.');
      return;
    }

    this.definitionForm.transitions = this.definitionForm.transitions.filter((_, i) => i !== index);
  }

  createDefinition(): void {
    if (!this.definitionForm.definitionKey.trim() || !this.definitionForm.name.trim()) {
      this.statusModalService.showWarning('Missing Data', 'Definition key and name are required.');
      return;
    }

    this.workflowAdminService.createDefinition(this.definitionForm).subscribe({
      next: (res) => {
        this.statusModalService.showSuccess('Workflow Created', res.message || 'Definition created successfully.');
        this.loadDefinitions();
      },
      error: (err: HttpErrorResponse) => {
        this.statusModalService.showError(
          'Create Failed',
          err.error?.message || 'Unable to create workflow definition.',
        );
      },
    });
  }

  activateDefinition(definition: WorkflowDefinitionResponse): void {
    this.workflowAdminService.activateDefinition(definition.definitionKey, definition.versionNumber).subscribe({
      next: (res) => {
        this.statusModalService.showSuccess('Workflow Activated', res.message || 'Definition activated successfully.');
        this.loadDefinitions();
      },
      error: (err: HttpErrorResponse) => {
        this.statusModalService.showError(
          'Activation Failed',
          err.error?.message || 'Unable to activate workflow definition.',
        );
      },
    });
  }

  loadDefinitions(): void {
    this.loadingDefinitions = true;
    this.workflowAdminService.listDefinitions(this.definitionFilter).subscribe({
      next: (res) => {
        this.definitions = res.data ?? [];
        this.loadingDefinitions = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loadingDefinitions = false;
        this.statusModalService.showError('Load Failed', err.error?.message || 'Unable to load workflow definitions.');
      },
    });
  }

  startInstance(): void {
    if (
      !this.startForm.definitionKey.trim() ||
      !this.startForm.referenceType.trim() ||
      !this.startForm.referenceId.trim()
    ) {
      this.statusModalService.showWarning(
        'Missing Data',
        'Definition key, reference type, and reference ID are required.',
      );
      return;
    }

    this.loadingInstance = true;
    this.workflowAdminService.startInstance(this.startForm).subscribe({
      next: (res) => {
        this.loadingInstance = false;
        this.currentInstance = res.data;
        this.selectedInstanceId = String(res.data.id);
        this.currentTasks = res.data.tasks ?? [];
        this.statusModalService.showSuccess(
          'Instance Started',
          res.message || 'Workflow instance started successfully.',
        );
      },
      error: (err: HttpErrorResponse) => {
        this.loadingInstance = false;
        this.statusModalService.showError('Start Failed', err.error?.message || 'Unable to start workflow instance.');
      },
    });
  }

  loadInstance(): void {
    const instanceId = Number(this.selectedInstanceId);
    if (!Number.isInteger(instanceId) || instanceId <= 0) {
      this.statusModalService.showWarning('Invalid ID', 'Enter a valid workflow instance ID.');
      return;
    }

    this.loadingInstance = true;
    this.workflowAdminService.getInstance(instanceId).subscribe({
      next: (res) => {
        this.currentInstance = res.data;
        this.currentTasks = res.data.tasks ?? [];
        this.loadingInstance = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loadingInstance = false;
        this.statusModalService.showError('Load Failed', err.error?.message || 'Unable to load workflow instance.');
      },
    });
  }

  transitionInstance(): void {
    const instanceId = Number(this.selectedInstanceId);
    if (!Number.isInteger(instanceId) || instanceId <= 0) {
      this.statusModalService.showWarning('Invalid ID', 'Enter a valid workflow instance ID.');
      return;
    }

    if (!this.transitionForm.actionLabel?.trim()) {
      this.statusModalService.showWarning('Missing Action', 'Transition action label is required.');
      return;
    }

    this.transitionLoading = true;
    this.workflowAdminService.transitionInstance(instanceId, this.transitionForm).subscribe({
      next: (res) => {
        this.currentInstance = res.data;
        this.currentTasks = res.data.tasks ?? [];
        this.transitionLoading = false;
        this.statusModalService.showSuccess('Transition Applied', res.message || 'Workflow transitioned successfully.');
      },
      error: (err: HttpErrorResponse) => {
        this.transitionLoading = false;
        this.statusModalService.showError(
          'Transition Failed',
          err.error?.message || 'Unable to transition workflow instance.',
        );
      },
    });
  }

  refreshTasks(): void {
    const instanceId = Number(this.selectedInstanceId);
    if (!Number.isInteger(instanceId) || instanceId <= 0) {
      this.statusModalService.showWarning('Invalid ID', 'Enter a valid workflow instance ID.');
      return;
    }

    this.workflowAdminService.getInstanceTasks(instanceId).subscribe({
      next: (res) => {
        this.currentTasks = res.data ?? [];
      },
      error: (err: HttpErrorResponse) => {
        this.statusModalService.showError('Task Load Failed', err.error?.message || 'Unable to load workflow tasks.');
      },
    });
  }
}
