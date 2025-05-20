import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { CommentBoxComponent } from '../comment-box/comment-box.component.js';
import { SideBySideDiffComponent } from 'ngx-diff';
import { NgFor } from '@angular/common';

@Component({
  selector: 'app-review-detail',
  imports: [FormsModule, SideBySideDiffComponent, CommentBoxComponent, NgFor],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  rawDiff: string = '';

  fileContents: Map<string, string[]> = new Map();

  existedComments: GerritCommentInfo[] = [];

  draftComments: GerritCommentInput[] = [];

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService
  ) {}

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;

    this.reviewSvc
      .getExistedComments(this.gerritChangeId)
      .subscribe((c) => (this.existedComments = c));
    this.reviewSvc.getDraftComments(this.gerritChangeId).subscribe((d) => {
      this.draftComments = d;
      this.fetchRawPatch();
    });

    this.reviewSvc.getChangedFileNames(this.gerritChangeId).subscribe((f) => {
      console.log('changed files: ', f);
    });

    this.reviewSvc
      .getChangedFileContents(this.gerritChangeId)
      .subscribe((f) => {
        console.log('changed file contents: ', f);

        this.fileContents = new Map(Object.entries(f));
      });
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }

  private fetchRawPatch() {
    this.reviewSvc
      .getChangeDiffs(this.gerritChangeId)
      .subscribe((p) => (this.rawDiff = p));
  }

  /**
   * Called whenever the user clicks a line in the diff.
   * We get { file, line, side } and can open a new draft.
   */
  onLineClick(evt: any) {
    const { file, line, side } = evt;
    // avoid duplicate drafts:
    if (this.draftComments.find((d) => d.path === file && d.line === line))
      return;

    const draft: GerritCommentInput = { path: file, line, side, message: '' };
    this.draftComments.push(draft);
  }

  saveComment(c: GerritCommentInput) {
    this.reviewSvc.postDraftComment(this.gerritChangeId, c).subscribe(() => {
      // once saved, you might reload existedComments and drop this draft
      this.draftComments = this.draftComments.filter((d) => d !== c);
    });
  }

  cancelComment(c: GerritCommentInput) {
    this.draftComments = this.draftComments.filter((d) => d !== c);
  }
}
