import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommentInput } from '../../interface/comment-input';
import { FormsModule } from '@angular/forms';
import { GerritService } from '../../http/gerrit.service';
import { CommentRange } from '../../interface/comment-range';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgIf],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() changeId: string = '';
  @Input() revisionId: string = '';
  @Input() selectedFile: string = '';
  @Input() selectedSide: 'PARENT' | 'REVISION' = 'REVISION';
  @Input() selectedLineNum: number | undefined = undefined;

  @Input() commentMsg: string = '';
  @Input() edittable: boolean = true;

  @Output() closeCommentBox = new EventEmitter<void>();

  constructor(private gerritSvc: GerritService) {}

  makeDraftComment(message: string, lineRange?: CommentRange) {
    if (
      !this.selectedFile ||
      !this.selectedSide ||
      this.selectedLineNum === undefined
    ) {
      console.error('Invalid comment input');
      return;
    }

    // Create CommentInput object
    const draftComment: CommentInput = {
      path: this.selectedFile,
      side: this.selectedSide,
      line: this.selectedLineNum,
      range: lineRange,
      message: message,
    };

    // Post draft comment
    this.gerritSvc
      .putDraftComment(this.changeId, this.revisionId, draftComment)
      .subscribe((data) => {
        console.log('Draft comment posted:', data);
      });
  }

  onCancel() {
    this.commentMsg = '';
    this.closeCommentBox.emit();
  }
}
