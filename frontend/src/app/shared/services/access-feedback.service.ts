import { BehaviorSubject } from 'rxjs';
import { Injectable } from '@angular/core';

export interface AccessFeedbackState {
  title: string;
  message: string;
}

@Injectable({
  providedIn: 'root',
})
export class AccessFeedbackService {
  private readonly modalSubject = new BehaviorSubject<AccessFeedbackState | null>(null);
  readonly modal$ = this.modalSubject.asObservable();

  showUnauthorized(message = 'You do not have permission to open that page. Redirecting you to your workspace.'): void {
    this.modalSubject.next({
      title: 'Unauthorized Access',
      message,
    });
  }

  close(): void {
    this.modalSubject.next(null);
  }
}






