import { Component, QueryList, ViewChildren } from '@angular/core';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../http/review.service';
import { AuthorDiffTableComponent } from '../author-diff-table/author-diff-table.component';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { AuthorPublishDialogComponent } from '../author-publish-dialog/author-publish-dialog.component';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';

@Component({
  selector: 'app-author-review-detail',
  imports: [
    AuthorDiffTableComponent,
    DatePipe,
    NgFor,
    AuthorPublishDialogComponent,
    NgIf,
  ],
  templateUrl: './author-review-detail.component.html',
  styleUrl: './author-review-detail.component.css',
})
export class AuthorReviewDetailComponent {
  gerritChangeId!: string;

  pseudoCommitDto!: PseudonymGitlabCommitDto;

  fileContents: Map<string, string[]> = new Map();

  assignmentId!: string;

  @ViewChildren(AuthorDiffTableComponent)
  diffTables!: QueryList<AuthorDiffTableComponent>;

  showPublishDialog = false;

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService
  ) {}

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;
    this.assignmentId = this.route.snapshot.paramMap.get('assignmentId')!;

    this.reviewSvc
      .getChangedFileContents(this.gerritChangeId)
      .subscribe((f) => {
        console.log('changed file contents: ', f);

        this.fileContents = new Map(Object.entries(f));
      });

    this.reviewSvc
      .getAuthorPseudonymCommit(this.gerritChangeId)
      .subscribe((p) => {
        this.pseudoCommitDto = p;
      });
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }

  // open the single parent dialog
  onOpenPublishDialog() {
    this.showPublishDialog = true;
  }

  // when the user confirms in that dialog...
  onPublishConfirmed() {
    this.showPublishDialog = false;

    // gather *all* draft-IDs from every child table
    const allDrafts: GerritCommentInput[] = this.diffTables
      .toArray()
      .flatMap((table) => table.draftComments);

    if (allDrafts.length === 0) {
      console.log('Nothing to publish');
      return;
    }

    // call your service once, with the union of every draft
    this.reviewSvc
      .publishAuthorDraftComments(
        this.gerritChangeId,
        this.assignmentId,
        allDrafts
      )
      .subscribe(() => {
        // tell each child to re-fetch its drafts & published comments
        this.diffTables.forEach((table) => {
          table.fetchDraftComments();
          table.fetchExistedComments();
        });
      });
  }

  getAllDraftCount(): number {
    return this.diffTables
      .toArray()
      .reduce((acc, table) => acc + table.draftComments.length, 0);
  }
}
