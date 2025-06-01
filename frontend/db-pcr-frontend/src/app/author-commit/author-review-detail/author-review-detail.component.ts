import { Component, QueryList, ViewChildren } from '@angular/core';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../http/review.service';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { AuthorPublishDialogComponent } from '../author-publish-dialog/author-publish-dialog.component';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';
import { DiffTableComponent } from '../../review/diff-table/diff-table.component';

@Component({
  selector: 'app-author-review-detail',
  imports: [
    DatePipe,
    NgFor,
    AuthorPublishDialogComponent,
    NgIf,
    DiffTableComponent,
  ],
  templateUrl: './author-review-detail.component.html',
  styleUrl: './author-review-detail.component.css',
})
export class AuthorReviewDetailComponent {
  gerritChangeId!: string;

  pseudoCommitDto!: PseudonymGitlabCommitDto;

  fileContents: Map<string, string[]> = new Map();

  assignmentId!: string;

  @ViewChildren(DiffTableComponent)
  diffTables!: QueryList<DiffTableComponent>;

  showPublishDialog = false;
  isLeavingPage = false;

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService,
    private router: Router
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
    this.isLeavingPage = false;
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
        // 4a) Refresh every child table exactly as before:
        this.diffTables.forEach((table) => {
          table.fetchDraftComments();
          table.fetchExistedComments();
        });

        // 4b) If we opened this dialog because “Back to Commit List” was clicked
        //     (i.e. isDialogLeaving === true), then navigate now:
        if (this.isLeavingPage) {
          this.router.navigate([
            '/commit-list',
            this.pseudoCommitDto.commit.projectId,
          ]);
        }
        // If isDialogLeaving is false, we do nothing else and remain on this page.
      });
  }

  getAllDraftCount(): number {
    return this.diffTables
      .toArray()
      .reduce((acc, table) => acc + table.draftComments.length, 0);
  }

  navigateToCommitList() {
    const drafts = this.getAllDraftCount();
    if (drafts > 0) {
      // Open dialog in “leaving” variant
      this.isLeavingPage = true;
      this.showPublishDialog = true;
    } else {
      // No drafts → go straight back
      this.router.navigate([
        '/commit-list',
        this.pseudoCommitDto.commit.projectId,
      ]);
    }
  }
}
