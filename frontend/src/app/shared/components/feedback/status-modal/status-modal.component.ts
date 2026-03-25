import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { StatusModalService, StatusModalState } from '../../../../core/services/status-modal.service';

@Component({
  selector: 'app-status-modal',
  standalone: true,
  imports: [CommonModule, DialogModule, ButtonModule],
  templateUrl: './status-modal.component.html',
  styleUrl: './status-modal.component.scss'
})
export class StatusModalComponent implements OnInit {
  
  visible = false;
  state: StatusModalState = { visible: false, type: 'success', title: '', message: '' };

  constructor(private statusModalService: StatusModalService) {}

  ngOnInit(): void {
    this.statusModalService.state$.subscribe((state) => {
      this.state = state;
      this.visible = state.visible;
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'success':
        return 'ri-checkbox-circle-fill';
      case 'error':
        return 'ri-error-warning-fill';
      case 'warning':
        return 'ri-alert-fill';
      default:
        return 'ri-information-fill';
    }
  }

  onHide(): void {
    this.visible = false;
    this.statusModalService.hide();
  }
}
