import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommentBoxComponent } from '../comment-box/comment-box.component';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';

export type PublishAction = 'resolve' | 'approve';

@Component({
  selector: 'app-publish-dialog',
  imports: [FormsModule],
  templateUrl: './publish-dialog.component.html',
  styleUrl: './publish-dialog.component.css',
})
export class PublishDialogComponent {
  @Input() count = 0;

  /** emits the chosen action + message */
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
