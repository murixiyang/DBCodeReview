import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe, NgSwitch, NgSwitchCase } from '@angular/common';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';

export type ReviewerCommentVariant =
  | 'published'
  | 'draft'
  | 'new'
  | 'update'
  | 'placeholder';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule, NgSwitch, NgSwitchCase, DatePipe],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() variant!: ReviewerCommentVariant;

  @Input() commenterName: string = '';

  /** Either a draft input or an existing comment */
  @Input() comment!: GerritCommentInput | GerritCommentInfo;

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

  @ViewChild('autosize') autosizeTextarea!: ElementRef<HTMLTextAreaElement>;

  /** call on each input to let it grow as needed */
  autoResize(textarea: HTMLTextAreaElement) {
    textarea.style.height = 'auto'; // reset to shrink if content was removed
    textarea.style.height = textarea.scrollHeight + 'px'; // expand to fit all text
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

  constructor() {}
}
