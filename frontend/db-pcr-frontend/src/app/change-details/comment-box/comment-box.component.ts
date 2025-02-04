import { Component, Input } from '@angular/core';
import { CommentRange } from '../../interface/comment-input';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-comment-box',
  imports: [FormsModule],
  templateUrl: './comment-box.component.html',
  styleUrl: './comment-box.component.css',
})
export class CommentBoxComponent {
  @Input() selectedFile: string = '';
  @Input() selectedSide: 'PARENT' | 'REVISION' | undefined = undefined;
  @Input() selectedLineNum: number | undefined = undefined;

  commentMsg: string = '';

  constructor() {}

  makeDraftComment(message: string, lineRange?: CommentRange) {
    console.log(
      'Draft comment:',
      message,
      this.selectedSide,
      this.selectedLineNum,
      lineRange
    );
  }
}
