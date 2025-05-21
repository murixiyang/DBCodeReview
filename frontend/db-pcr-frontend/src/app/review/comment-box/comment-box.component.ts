import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf, NgSwitch, NgSwitchCase } from '@angular/common';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';

export type CommentVariant = 'published' | 'draft' | 'new' | 'placeholder';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgSwitch, NgSwitchCase],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() variant!: CommentVariant;

  /** Either a draft input or an existing comment */
  @Input() comment!: GerritCommentInput | GerritCommentInfo;

  /** save (for new & draft) */
  @Output() saved = new EventEmitter<GerritCommentInput>();

  /** cancel (for new & draft) */
  @Output() canceled = new EventEmitter<void>();

  /** delete (for draft) */
  @Output() deleted = new EventEmitter<GerritCommentInput>();

  /** reply (for published) */
  @Output() reply = new EventEmitter<GerritCommentInfo>();

  onSave() {
    if (this.comment && 'message' in this.comment) {
      this.saved.emit(this.comment as GerritCommentInput);
    }
  }

  onCancel() {
    this.canceled.emit();
  }

  onDelete() {
    if (this.comment && 'message' in this.comment) {
      this.deleted.emit(this.comment as GerritCommentInput);
    }
  }

  onReply() {
    if (this.comment && 'message' in this.comment) {
      this.reply.emit(this.comment as GerritCommentInfo);
    }
  }

  constructor() {}
}
