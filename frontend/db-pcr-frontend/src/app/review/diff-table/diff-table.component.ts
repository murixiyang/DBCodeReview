import { NgClass, NgFor, NgIf } from '@angular/common';
import {
  Component,
  ElementRef,
  Input,
  OnChanges,
  QueryList,
  SimpleChanges,
  ViewChildren,
} from '@angular/core';
import { DiffMatchPatch, DiffOp } from 'diff-match-patch-ts';
import { CommentBoxComponent } from '../comment-box/comment-box.component';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { ReviewService } from '../../http/review.service';
import { PublishDialogComponent } from '../publish-dialog/publish-dialog.component';
import { NameCommentInfo } from '../../interface/gerrit/name-comment-info';
import { DiffLine } from '../../interface/gerrit/diff-line';
import { PublishAction } from '../../interface/publish-action';

@Component({
  selector: 'app-diff-table',
  imports: [NgFor, NgIf, CommentBoxComponent, NgClass, PublishDialogComponent],
  templateUrl: './diff-table.component.html',
  styleUrl: './diff-table.component.css',
})
export class DiffTableComponent implements OnChanges {
  @Input() selectedAssignmentId!: string;
  @Input() gerritChangeId!: string;
  @Input() oldText!: string;
  @Input() newText!: string;
  @Input() file!: string;

  existedComments: NameCommentInfo[] = [];
  overallComments: NameCommentInfo[] = [];
  draftComments: GerritCommentInput[] = [];

  lines: DiffLine[] = [];

  selectedIndex: number | null = null;
  newDraft?: GerritCommentInput;
  editingDraft?: GerritCommentInput;

  replyingTo?: GerritCommentInfo;
  replyDraft?: GerritCommentInput;

  // Header
  insertedCount = 0;
  deletedCount = 0;

  // Placeholder commentbox
  @ViewChildren('measureRow', { read: ElementRef })
  measuredRows!: QueryList<ElementRef<HTMLTableRowElement>>;
  /** placeholderHeights[i] = total height (px) of all comment rows for line i */
  placeholderHeights: number[] = [];

  showPublishDialog = false;

  constructor(private reviewSvc: ReviewService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['oldText'] || changes['newText']) {
      this.lines = this.buildLines(this.oldText, this.newText);
      this.updateHeaderInfo();
    }
  }

  ngOnInit() {
    this.fetchExistedComments();
    this.fetchDraftComments();
  }

  // ---- To align placehoder comment box ----- //
  ngAfterViewInit() {
    // initial compute
    this.updatePlaceholderHeights();

    // recompute whenever the set of rows changes (e.g. user adds/deletes a comment)
    this.measuredRows.changes.subscribe(() => {
      // Give the DOM a tick to finish rendering
      setTimeout(() => this.updatePlaceholderHeights());
    });
  }

  private updatePlaceholderHeights() {
    // reset
    this.placeholderHeights = [];

    // group & sum
    this.measuredRows.forEach((rowEl) => {
      const lineIdx = Number(rowEl.nativeElement.dataset['line']);
      const h = rowEl.nativeElement.getBoundingClientRect().height;
      this.placeholderHeights[lineIdx] =
        (this.placeholderHeights[lineIdx] || 0) + h;
    });
  }

  fetchExistedComments() {
    this.reviewSvc
      .getExistedCommentsWithPseudonym(this.gerritChangeId)
      .subscribe((c) => {
        // Overall comments are always at line 0
        this.overallComments = c.filter(
          (c) =>
            c.commentInfo.line === 0 || c.commentInfo.path === '/PATCHSET_LEVEL'
        );

        // filter and sort
        const filtered = c.filter(
          (comment) => comment.commentInfo.path === this.file
        );
        this.existedComments = filtered.sort((a, b) => {
          const ta = new Date(a.commentInfo.updated ?? Date.now()).getTime();
          const tb = new Date(b.commentInfo.updated ?? Date.now()).getTime();
          return ta - tb;
        });

        console.log('existed comments: ', c);
      });
  }

  fetchDraftComments() {
    this.reviewSvc.getUserDraftComments(this.gerritChangeId).subscribe((d) => {
      const filtered = d.filter((draft) => draft.path === this.file);
      this.draftComments = filtered.sort((a, b) => {
        const ta = new Date(a.updated ?? Date.now()).getTime();
        const tb = new Date(b.updated ?? Date.now()).getTime();
        return ta - tb;
      });

      console.log('draft comments: ', d);
    });
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
  ): NameCommentInfo[] {
    return this.existedComments.filter(
      (c) =>
        c.commentInfo.path === path &&
        c.commentInfo.line === line &&
        c.commentInfo.side === side
    );
  }

  // called when user clicks a code row
  selectLine(idx: number) {
    const line = this.lines[idx];
    const ln = line.newNumber ?? line.oldNumber!;
    this.selectedIndex = idx;
    this.newDraft = { path: this.file, line: ln, message: '' };
  }

  // Get commenter's display name
  getDisplayName(comment: NameCommentInfo): string {
    if (comment.isAuthor) {
      return comment.displayName + ' (Author)';
    } else {
      return comment.displayName;
    }
  }

  onSaveDraft(draft: GerritCommentInput, side?: 'PARENT' | 'REVISION') {
    draft.side = side;

    console.log('Save draft: ', draft);
    this.reviewSvc
      .postReviewerDraftComment(
        this.gerritChangeId,
        this.selectedAssignmentId,
        draft
      )
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
    this.reviewSvc
      .deleteDraftComment(this.gerritChangeId, this.selectedAssignmentId, d)
      .subscribe(() => {
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
      .postReviewerDraftComment(
        this.gerritChangeId,
        this.selectedAssignmentId,
        draft
      )
      .subscribe(() => {
        this.onCancelReply();
        this.fetchDraftComments();
      });
  }

  onCancelReply() {
    this.replyingTo = undefined;
    this.replyDraft = undefined;
  }

  onOpenPublishDialog() {
    this.showPublishDialog = true;
  }

  // Post the draft comments to the server
  onPublishConfirmed(evt: { action: PublishAction }) {
    this.showPublishDialog = false;

    if (this.draftComments.length === 0) {
      console.log('No draft comments to publish');
      return;
    }

    // gather the IDs you want to publish
    const draftIds = this.draftComments.map((d) => d.id!);

    // Compute if need resolve
    const needResolve = evt.action === 'resolve';

    // call your service, passing along the overall message if you like
    this.reviewSvc
      .publishReviewerDraftComments(
        this.gerritChangeId,
        this.selectedAssignmentId,
        needResolve,
        draftIds
      )
      .subscribe(() => {
        console.log('Publish draft comments');
        this.fetchDraftComments();
        this.fetchExistedComments();
      });
  }
}
