import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { GerritService } from '../../../http/gerrit.service';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgIf],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() changeId: string = '';
  @Input() revisionId: string = '';

  // Used when creating a new draft comment
  @Input() newCommentInput: CommentInput | undefined = undefined;
  @Input() commentMsg: string = '';

  // Used when show existed draft comment
  @Input() existCommentInfo: CommentInfo | undefined = undefined;

  @Input() edittable: boolean = true;

  editting: boolean = false;

  @Output() closeCommentBox = new EventEmitter<void>();

  constructor(private gerritSvc: GerritService) {}

  onSaveDraft(): void {
    // If editting a existed comment
    if (this.editting && this.existCommentInfo) {
      this.onUpdateDraft(this.commentInfoToCommentInput(this.existCommentInfo));
      return;
    }

    // Creating new comment
    if (!this.newCommentInput) {
      console.error('Invalid comment input');
      return;
    }

    // Change message
    this.newCommentInput.message = this.commentMsg;

    // Post draft comment
    this.gerritSvc
      .putDraftComment(this.changeId, this.revisionId, this.newCommentInput)
      .subscribe((data) => {
        console.log('Draft comment posted:', data);

        // Close comment box
        this.onCloseDraft();
      });
  }

  onUpdateDraft(oldCommentInput: CommentInput): void {
    oldCommentInput.message = this.commentMsg;

    // Post draft comment
    this.gerritSvc
      .updateDraftComment(this.changeId, this.revisionId, oldCommentInput)
      .subscribe((data) => {
        console.log('Draft comment updated:', data);

        // Close comment box
        this.onCloseDraft();
      });
  }

  onCloseDraft() {
    this.commentMsg = '';
    this.editting = false;
    this.closeCommentBox.emit();
  }

  onEditDraft(): void {
    this.edittable = true;
    this.commentMsg = this.existCommentInfo?.message || '';
    this.editting = true;
  }

  onDeleteDraft() {
    if (!this.existCommentInfo) {
      console.error('Invalid existed comment info');
      return;
    }

    if (!this.existCommentInfo.id) {
      console.error('Invalid comment ID');
    }

    this.gerritSvc
      .deleteDraftComment(
        this.changeId,
        this.revisionId,
        this.existCommentInfo.id
      )
      .subscribe((data) => {
        console.log('Draft comment deleted:', data);

        // Close comment box
        this.onCloseDraft();
      });
  }

  private commentInfoToCommentInput(commentInfo: CommentInfo): CommentInput {
    return {
      id: commentInfo.id,
      path: commentInfo.path,
      side: commentInfo.side,
      line: commentInfo.line,
      range: commentInfo.range,
      in_reply_to: commentInfo.in_reply_to,
      message: commentInfo.message,
      updated: commentInfo.updated,
    };
  }
}
