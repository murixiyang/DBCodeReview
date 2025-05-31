import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgIf, NgSwitch, NgSwitchCase } from '@angular/common';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';

import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ReactState } from '../../interface/react-state';

export type ReviewerCommentVariant =
  | 'published'
  | 'draft'
  | 'new'
  | 'update'
  | 'placeholder';

@Component({
  selector: 'app-comment-box',
  imports: [
    FormsModule,
    NgSwitch,
    NgSwitchCase,
    DatePipe,
    NgIf,
    MatIconModule,
    MatButtonModule,
  ],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() variant!: ReviewerCommentVariant;

  @Input() commenterName: string = '';

  /** Either a draft input or an existing comment */
  @Input() comment!: GerritCommentInput | GerritCommentInfo;

  // Whtether the comment publisher is author (author can thumb up/down)
  @Input() isAuthor = false;

  // Whtether this comment is from the current user
  @Input() ownComment = false;

  /** track what the *current* user has done */
  userReaction: ReactState = ReactState.NONE;

  /** save (for new & draft) */
  @Output() saved = new EventEmitter<GerritCommentInput>();

  /** edit (for draft) */
  @Output() edited = new EventEmitter<GerritCommentInput>();

  /** cancel (for new ) */
  @Output() canceled = new EventEmitter<void>();

  /** delete (for draft) */
  @Output() deleted = new EventEmitter<GerritCommentInput>();

  /** reply (for published) */
  @Output() reply = new EventEmitter<GerritCommentInfo>();

  /** author react (for published) */
  @Output() react = new EventEmitter<{ id: string; type: ReactState }>();

  @ViewChild('autosize') autosizeTextarea!: ElementRef<HTMLTextAreaElement>;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['comment']) {
      this.setUserReactionFromComment();
    }
  }

  private setUserReactionFromComment() {
    if (this.isGerritCommentInfo(this.comment)) {
      this.userReaction = this.comment.thumbState
        ? this.comment.thumbState
        : ReactState.NONE;
    } else {
      this.userReaction = ReactState.NONE;
    }
  }

  private isGerritCommentInfo(
    comment: GerritCommentInput | GerritCommentInfo
  ): comment is GerritCommentInfo {
    return 'thumbState' in comment;
  }

  /** call on each input to let it grow as needed */
  autoResize(textarea: HTMLTextAreaElement) {
    textarea.style.height = 'auto'; // reset to shrink if content was removed
    textarea.style.height = textarea.scrollHeight + 'px'; // expand to fit all text
  }

  getDraftCommenterName(): string {
    if (!this.commenterName) {
      return 'Me';
    }
    return this.commenterName;
  }

  onSave() {
    if (this.comment && 'message' in this.comment) {
      this.saved.emit(this.comment as GerritCommentInput);
    }
  }

  onEdit() {
    if (this.comment && 'message' in this.comment) {
      this.edited.emit(this.comment as GerritCommentInput);
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

  onReactUp() {
    this.userReaction =
      this.userReaction === ReactState.UP ? ReactState.NONE : ReactState.UP;
    this.react.emit({ id: this.comment.id!, type: this.userReaction });
  }

  onReactDown() {
    this.userReaction =
      this.userReaction === ReactState.DOWN ? ReactState.NONE : ReactState.DOWN;
    this.react.emit({
      id: this.comment.id!,
      type: this.userReaction,
    });
  }

  constructor() {}
}
