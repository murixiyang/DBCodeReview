import { NgClass, NgFor, NgIf } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { DiffMatchPatch, DiffOp } from 'diff-match-patch-ts';
import { CommentBoxComponent } from '../comment-box/comment-box.component';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { ReviewService } from '../../http/review.service';

interface DiffLine {
  oldNumber: number | null;
  newNumber: number | null;
  text: string;
  type: 'equal' | 'insert' | 'delete';
}

@Component({
  selector: 'app-diff-table',
  imports: [NgFor, NgIf, CommentBoxComponent, NgClass],
  templateUrl: './diff-table.component.html',
  styleUrl: './diff-table.component.css',
})
export class DiffTableComponent implements OnChanges {
  @Input() gerritChangeId!: string;
  @Input() oldText!: string;
  @Input() newText!: string;
  @Input() existedComments!: GerritCommentInfo[];
  @Input() draftComments!: GerritCommentInput[];
  @Input() file!: string;

  lines: DiffLine[] = [];

  constructor(private reviewSvc: ReviewService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oldText'] || changes['newText']) {
      this.lines = this.buildLines(this.oldText, this.newText);

      console.log('oldText: ', this.oldText);
      console.log('newText: ', this.newText);
    }
  }

  ngOnInit() {}

  buildLines(oldText: string, newText: string): DiffLine[] {
    const dmp = new DiffMatchPatch();
    const ops = dmp.diff_main(oldText, newText);
    dmp.diff_cleanupSemantic(ops);

    const lines: DiffLine[] = [];
    let oldNum = 1,
      newNum = 1;

    ops.forEach((op) => {
      const [kind, chunk] = op;
      const type =
        kind === DiffOp.Equal
          ? 'equal'
          : kind === DiffOp.Insert
          ? 'insert'
          : 'delete';
      const segments = chunk.split('\n');
      segments.forEach((segment) => {
        lines.push({
          oldNumber: type === 'insert' ? null : oldNum++,
          newNumber: type === 'delete' ? null : newNum++,
          text: segment,
          type,
        });
      });
    });

    return lines;
  }

  hasComments(newNumber: number | null): boolean {
    if (newNumber === null) return false;
    const pub = this.publishedFor(this.file, newNumber);
    const draft = this.draftFor(this.file, newNumber);
    return (pub && pub.length > 0) || !!draft;
  }

  /** Find the single draft for that file+line (or undefined) */
  draftFor(path: string, line: number): GerritCommentInput[] | undefined {
    return this.draftComments.filter((d) => d.path === path && d.line === line);
  }

  /** Find all published comments at that file+line */
  publishedFor(path: string, line: number): GerritCommentInfo[] {
    return this.existedComments.filter(
      (c) => c.path === path && c.line === line
    );
  }
}
