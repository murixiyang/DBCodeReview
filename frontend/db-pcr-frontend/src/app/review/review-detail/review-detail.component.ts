import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { CommentBoxComponent } from '../comment-box/comment-box.component.js';
import { SideBySideDiffComponent } from 'ngx-diff';
import { NgFor, NgIf, NgSwitch } from '@angular/common';

@Component({
  selector: 'app-review-detail',
  imports: [
    FormsModule,
    SideBySideDiffComponent,
    CommentBoxComponent,
    NgFor,
    NgIf,
  ],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  fileContents: Map<string, string[]> = new Map();

  existedComments: GerritCommentInfo[] = [];

  draftComments: GerritCommentInput[] = [];

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService
  ) {}

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;

    this.reviewSvc.getExistedComments(this.gerritChangeId).subscribe((c) => {
      this.existedComments = c;
      console.log('existed comments: ', c);
    });

    this.reviewSvc.getDraftComments(this.gerritChangeId).subscribe((d) => {
      this.draftComments = d;
      console.log('draft comments: ', d);
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

  /** Find the single draft for that file+line (or undefined) */
  draftFor(path: string, line: number): GerritCommentInput | undefined {
    return this.draftComments.find((d) => d.path === path && d.line === line);
  }

  /** Find all published comments at that file+line */
  publishedFor(path: string, line: number): GerritCommentInfo[] {
    return this.existedComments.filter(
      (c) => c.path === path && c.line === line
    );
  }

  onLineClick(evt: any) {
    const { file, line, side } = evt;
    // don’t double-add
    if (!this.draftFor(file, line)) {
      this.draftComments.push({ path: file, line, side, message: '' });
    }
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
