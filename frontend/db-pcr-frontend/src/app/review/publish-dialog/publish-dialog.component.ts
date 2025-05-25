import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PublishAction } from '../../interface/publish-action';

@Component({
  selector: 'app-publish-dialog',
  imports: [FormsModule],
  templateUrl: './publish-dialog.component.html',
  styleUrl: './publish-dialog.component.css',
})
export class PublishDialogComponent {
  @Input() count = 0;

  /** emits the chosen action */
  @Output() confirm = new EventEmitter<{ action: PublishAction }>();
  /** cancel button or clicking outside */
  @Output() cancel = new EventEmitter<void>();

  onConfirm(action: PublishAction) {
    this.confirm.emit({ action });
  }

  onCancel() {
    this.cancel.emit();
  }
}
