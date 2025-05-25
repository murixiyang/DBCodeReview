import { NgIf } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-author-publish-dialog',
  imports: [FormsModule, NgIf],
  templateUrl: './author-publish-dialog.component.html',
  styleUrl: './author-publish-dialog.component.css',
})
export class AuthorPublishDialogComponent {
  @Input() count = 0;

  /** emits the chosen action + message */
  @Output() confirm = new EventEmitter<void>();
  /** cancel button or clicking outside */
  @Output() cancel = new EventEmitter<void>();

  onConfirm() {
    this.confirm.emit();
  }

  onCancel() {
    this.cancel.emit();
  }
}
