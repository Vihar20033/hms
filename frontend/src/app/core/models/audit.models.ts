export interface AuditLog {
  id: number;
  username: string;
  action: string;
  entityType?: string;
  entityId?: string;
  details?: string;
  createdAt: string;
}
