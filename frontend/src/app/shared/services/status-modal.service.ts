import { BehaviorSubject, Observable, of } from 'rxjs';
import { Injectable } from '@angular/core';

export type StatusType = 'success' | 'error' | 'info' | 'warning';

export interface StatusModalState {
  visible: boolean;
  type: StatusType;
  title: string;
  message: string;
  confirmMode?: boolean;
  confirmLabel?: string;
  cancelLabel?: string;
}

@Injectable({
  providedIn: 'root',
})

// Service to manage the state of a status modal (success, error, info, warning)
export class StatusModalService {
  private confirmResolver?: (confirmed: boolean) => void;
  private state = new BehaviorSubject<StatusModalState>({
    visible: false,
    type: 'success',
    title: '',
    message: '',
  });

  state$: Observable<StatusModalState> = this.state.asObservable();

  showSuccess(title: string, message: string): void {
    this.state.next({ visible: true, type: 'success', title, message });
  }

  showError(title: string, message: string): void {
    this.state.next({ visible: true, type: 'error', title, message });
  }

  showInfo(title: string, message: string): void {
    this.state.next({ visible: true, type: 'info', title, message });
  }

  showWarning(title: string, message: string): void {
    this.state.next({ visible: true, type: 'warning', title, message });
  }

  confirm(title: string, message: string, confirmLabel = 'Confirm', cancelLabel = 'Cancel'): Promise<boolean> {
    this.confirmResolver?.(false);
    return new Promise<boolean>((resolve) => {
      this.confirmResolver = resolve;
      this.state.next({
        visible: true,
        type: 'warning',
        title,
        message,
        confirmMode: true,
        confirmLabel,
        cancelLabel,
      });
    });
  }

  resolveConfirm(confirmed: boolean): void {
    this.confirmResolver?.(confirmed);
    this.confirmResolver = undefined;
    this.hide();
  }

  hide(): void {
    this.confirmResolver?.(false);
    this.confirmResolver = undefined;
    this.state.next({ ...this.state.value, visible: false });
  }
}






