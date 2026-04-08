import { AccessFeedbackService } from '../../services/access-feedback.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-access-feedback-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './access-feedback-modal.component.html',
  styleUrl: './access-feedback-modal.component.scss',
})
export class AccessFeedbackModalComponent {
  
  readonly modal$ = this.accessFeedbackService.modal$;

  constructor(private accessFeedbackService: AccessFeedbackService) {}

  close(): void {
    this.accessFeedbackService.close();
  }
}









