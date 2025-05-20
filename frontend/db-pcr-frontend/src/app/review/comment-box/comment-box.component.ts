import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgIf],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  /** mode: 'draft' to edit, 'published' to read-only */
  @Input() mode!: 'draft' | 'published';

  /** Either a draft input or an existing comment */
  @Input() comment!: GerritCommentInput | GerritCommentInfo;

  /** Emitted when Save/Update clicked */
  @Output() saved = new EventEmitter<GerritCommentInput>();

  /** Emitted when Cancel or Discard clicked */
  @Output() canceled = new EventEmitter<GerritCommentInput>();

  onSaveDraft() {
    this.saved.emit(this.comment);
  }

  onCancelDraft() {
    this.canceled.emit(this.comment);
  }

  constructor() {}

  //   onSaveDraft(): void {
  //     // If editting a existed comment
  //     if (this.editting && this.existCommentInfo) {
  //       this.onUpdateDraft(this.commentInfoToCommentInput(this.existCommentInfo));
  //       return;
  //     }

  //     // Creating new comment
  //     if (!this.newCommentInput) {
  //       console.error('Invalid comment input');
  //       return;
  //     }

  //     // Change message
  //     this.newCommentInput.message = this.commentMsg;
  //   }

  //   onUpdateDraft(oldCommentInput: GerritCommentInput): void {
  //     oldCommentInput.message = this.commentMsg;
  //   }

  //   onCloseDraft() {
  //     this.commentMsg = '';
  //     this.editting = false;
  //     this.closeCommentBox.emit();
  //   }

  //   onEditDraft(): void {
  //     this.edittable = true;
  //     this.commentMsg = this.existCommentInfo?.message || '';
  //     this.editting = true;
  //   }
}
