import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommentInput } from '../../interface/comment-input';
import { FormsModule } from '@angular/forms';
import { GerritService } from '../../http/gerrit.service';
import { CommentRange } from '../../interface/comment-range';
import { NgIf } from '@angular/common';
import { CommentInfo } from '../../interface/comment-info';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgIf],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() changeId: string = '';
  @Input() revisionId: string = '';

  @Input() newCommentInput: CommentInput | undefined = undefined;
  @Input() commentMsg: string = '';

  @Input() existCommentInfo: CommentInfo | undefined = undefined;
  @Input() edittable: boolean = true;

  @Output() closeCommentBox = new EventEmitter<void>();

  constructor(private gerritSvc: GerritService) {}

  makeDraftComment(message: string, lineRange?: CommentRange) {
    if (!this.newCommentInput) {
      console.log('ERROR: Invalid newCommentInput');
      return;
    }

    // Create CommentInput object
    this.newCommentInput.message = message;

    // Post draft comment
    this.gerritSvc
      .putDraftComment(this.changeId, this.revisionId, this.newCommentInput)
      .subscribe((data) => {
        console.log('Draft comment posted:', data);

        // Close comment box
        this.closeComment();
      });
  }

  closeComment() {
    this.commentMsg = '';
    this.closeCommentBox.emit();
  }

  deleteDraft() {
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
        this.closeComment();
      });
  }
}
