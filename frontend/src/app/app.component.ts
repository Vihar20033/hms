import { AccessFeedbackModalComponent } from './shared/components/access-feedback-modal/access-feedback-modal.component';
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { StatusModalComponent } from './shared/components/status-modal/status-modal.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AccessFeedbackModalComponent, StatusModalComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  title = 'frontend';
}







