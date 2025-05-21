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
  oldText: string;
  newText: string;
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

  newDraft: GerritCommentInput | null = null;
  selectedIndex: number | null = null;

  constructor(private reviewSvc: ReviewService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oldText'] || changes['newText']) {
      this.lines = this.buildLines(this.oldText, this.newText);
    }
  }

  ngOnInit() {}

  buildLines(oldText: string, newText: string): DiffLine[] {
    const dmp = new DiffMatchPatch();

    // Convert texts to chars, mapping each unique line to a placeholder char
    const helpers = dmp as any;
    const { chars1, chars2, lineArray } = helpers.diff_linesToChars_(
      oldText,
      newText
    );

    // Compute diff on the char sequences (line-level)
    const ops = dmp.diff_main(chars1, chars2, false);
    // Optionally tidy up
    dmp.diff_cleanupSemantic(ops);
    // Rehydrate the lines from placeholders
    helpers.diff_charsToLines_(ops, lineArray);

    const lines: DiffLine[] = [];
    let oldNum = 1,
      newNum = 1;

    ops.forEach(([kind, chunk]) => {
      const type =
        kind === DiffOp.Equal
          ? 'equal'
          : kind === DiffOp.Insert
          ? 'insert'
          : 'delete';

      // Each chunk now contains full lines ending with "\n" except possibly last
      const segments = chunk.split('\n');
      // Last split is empty if chunk ended with newline
      segments.forEach((segment, idx) => {
        if (segment === '' && idx === segments.length - 1) return;
        lines.push({
          oldNumber: type === 'insert' ? null : oldNum++,
          newNumber: type === 'delete' ? null : newNum++,
          oldText: type === 'insert' ? '' : segment,
          newText: type === 'delete' ? '' : segment,
          type,
        });
      });
    });

    return lines;
  }

  hasComments(newNumber: number | null, side?: 'PARENT' | 'REVISION'): boolean {
    if (newNumber === null) return false;

    const published = this.publishedFor(this.file, newNumber, side) || [];
    const draft = this.draftFor(this.file, newNumber, side);

    return published.length > 0 || draft.length > 0;
  }

  /** Find the single draft for that file+line (or undefined) */
  draftFor(
    path: string,
    line: number,
    side?: 'PARENT' | 'REVISION'
  ): GerritCommentInput[] {
    return this.draftComments.filter(
      (d) => d.path === path && d.line === line && d.side === side
    );
  }

  /** Find all published comments at that file+line */
  publishedFor(
    path: string,
    line: number,
    side?: 'PARENT' | 'REVISION'
  ): GerritCommentInfo[] {
    return this.existedComments.filter(
      (c) => c.path === path && c.line === line && c.side === side
    );
  }

  selectLine(idx: number) {
    // toggle off if you click again
    if (this.selectedIndex === idx) {
      this.selectedIndex = null;
      this.newDraft = null;
    } else {
      this.selectedIndex = idx;
      const line = this.lines[idx];
      const ln = line.newNumber ?? line.oldNumber!;
      this.newDraft = { path: this.file, line: ln, message: '' };
    }
  }

  onSaveDraft(draft: GerritCommentInput) {
    this.reviewSvc
      .postDraftComment(this.gerritChangeId, draft)
      .subscribe(() => {
        this.selectedIndex = null;
        this.newDraft = null;
      });
  }

  cancelComment(c: GerritCommentInput) {
    this.selectedIndex = null;
    this.newDraft = null;
  }
}
