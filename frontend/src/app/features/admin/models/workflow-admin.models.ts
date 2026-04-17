export type WorkflowDomain = 'OPD' | 'IPD' | 'EMERGENCY' | 'SURGERY' | 'DISCHARGE' | 'REFERRAL';

export type WorkflowDefinitionStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE';
export type WorkflowInstanceStatus = 'RUNNING' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
export type WorkflowTaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED' | 'CANCELLED';

export interface WorkflowStepRequest {
  stepCode: string;
  name: string;
  stepOrder: number;
  assigneeRole?: string;
  slaMinutes?: number | null;
  terminalStep?: boolean;
}

export interface WorkflowTransitionRequest {
  fromStepCode: string;
  toStepCode: string;
  actionLabel: string;
  requiresApproval?: boolean;
  approvalRole?: string;
  conditionExpression?: string;
}

export interface CreateWorkflowDefinitionRequest {
  definitionKey: string;
  name: string;
  domain: WorkflowDomain;
  initialStepCode: string;
  description?: string;
  steps: WorkflowStepRequest[];
  transitions: WorkflowTransitionRequest[];
}

export interface StartWorkflowInstanceRequest {
  definitionKey: string;
  referenceType: string;
  referenceId: string;
  contextJson?: string;
}

export interface TransitionWorkflowRequest {
  actionLabel: string;
  approvalGranted?: boolean;
  notes?: string;
  assigneeUserId?: number | null;
}

export interface WorkflowStepResponse {
  stepCode: string;
  name: string;
  stepOrder: number;
  assigneeRole?: string;
  slaMinutes?: number;
  terminalStep: boolean;
}

export interface WorkflowDefinitionResponse {
  id: number;
  definitionKey: string;
  name: string;
  domain: WorkflowDomain;
  versionNumber: number;
  status: WorkflowDefinitionStatus;
  initialStepCode: string;
  description?: string;
  steps: WorkflowStepResponse[];
}

export interface WorkflowTaskResponse {
  id: number;
  stepCode: string;
  title: string;
  assigneeUserId?: number;
  assigneeRole?: string;
  status: WorkflowTaskStatus;
  startedAt?: string;
  completedAt?: string;
  dueAt?: string;
  notes?: string;
}

export interface WorkflowInstanceResponse {
  id: number;
  definitionKey: string;
  definitionVersion: number;
  domain: WorkflowDomain;
  referenceType: string;
  referenceId: string;
  currentStepCode: string;
  status: WorkflowInstanceStatus;
  startedAt: string;
  completedAt?: string;
  tasks: WorkflowTaskResponse[];
}
