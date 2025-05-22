import { NgClass, NgFor, NgIf } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { DiffMatchPatch, DiffOp } from 'diff-match-patch-ts';
import { CommentBoxComponent } from '../comment-box/comment-box.component';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { ReviewService } from '../../http/review.service';
import { filter } from 'rxjs';

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
  @Input() file!: string;

  existedComments: GerritCommentInfo[] = [];
  draftComments: GerritCommentInput[] = [];

  lines: DiffLine[] = [];

  selectedIndex: number | null = null;
  newDraft?: GerritCommentInput;
  editingDraft?: GerritCommentInput;

  replyingTo?: GerritCommentInfo;
  replyDraft?: GerritCommentInput;

  // these power the header
  insertedCount = 0;
  deletedCount = 0;

  constructor(private reviewSvc: ReviewService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oldText'] || changes['newText']) {
      this.lines = this.buildLines(this.oldText, this.newText);
      this.updateHeaderInfo();
    }
  }

  ngOnInit() {
    this.fetechExistedComments();
    this.fetchDraftComments();
  }

  fetechExistedComments() {
    this.reviewSvc.getExistedComments(this.gerritChangeId).subscribe((c) => {
      this.existedComments = this.filterCommentForFile(c);
      console.log('existed comments: ', c);
    });
  }

  fetchDraftComments() {
    this.reviewSvc.getDraftComments(this.gerritChangeId).subscribe((d) => {
      this.draftComments = this.filterDraftForFile(d);
      console.log('draft comments: ', d);
    });
  }

  filterCommentForFile(comments: GerritCommentInfo[]): GerritCommentInfo[] {
    return comments.filter((c) => c.path === this.file);
  }

  filterDraftForFile(comments: GerritCommentInput[]): GerritCommentInput[] {
    return comments.filter((c) => c.path === this.file);
  }

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

  updateHeaderInfo() {
    // totals
    this.insertedCount = this.lines.filter((l) => l.type === 'insert').length;
    this.deletedCount = this.lines.filter((l) => l.type === 'delete').length;
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

  // called when user clicks a code row
  selectLine(idx: number) {
    const line = this.lines[idx];
    const ln = line.newNumber ?? line.oldNumber!;
    this.selectedIndex = idx;
    this.newDraft = { path: this.file, line: ln, message: '' };
  }

  onSaveDraft(draft: GerritCommentInput, side?: 'PARENT' | 'REVISION') {
    draft.side = side;

    console.log('Save draft: ', draft);
    this.reviewSvc
      .postDraftComment(this.gerritChangeId, draft)
      .subscribe((savedDraft: GerritCommentInput) => {
        this.selectedIndex = null;
        this.newDraft = undefined;

        this.fetchDraftComments();
      });
  }

  onCancelDraft() {
    this.selectedIndex = null;
    this.newDraft = undefined;
    this.editingDraft = undefined;
  }

  onEditDraft(d: GerritCommentInput) {
    console.log('Edit draft: ', d);
    this.editingDraft = { ...d };
  }

  onUpdateDraft(updated: GerritCommentInput) {
    console.log('Update draft: ', updated);

    this.reviewSvc
      .updateDraftComment(this.gerritChangeId, updated)
      .subscribe(() => {
        this.fetchDraftComments();
        this.editingDraft = undefined;
      });
  }

  onCancelUpdate() {
    this.editingDraft = undefined;
  }

  onDeleteDraft(d: GerritCommentInput) {
    this.reviewSvc.deleteDraftComment(this.gerritChangeId, d).subscribe(() => {
      console.log('Delete draft: ', d);
      this.fetchDraftComments();
    });
  }

  onReply(c: GerritCommentInfo, side: 'PARENT' | 'REVISION') {
    this.replyingTo = c;

    this.replyDraft = {
      path: c.path,
      line: c.line,
      message: '',
      side,
      inReplyTo: c.id,
    };
  }

  onSaveReply(draft: GerritCommentInput) {
    this.reviewSvc
      .postDraftComment(this.gerritChangeId, draft)
      .subscribe(() => {
        this.onCancelReply();
        this.fetchDraftComments();
      });
  }

  onCancelReply() {
    this.replyingTo = undefined;
    this.replyDraft = undefined;
  }
}
