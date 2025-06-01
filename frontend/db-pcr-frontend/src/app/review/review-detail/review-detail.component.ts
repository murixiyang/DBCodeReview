import { Component, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { DiffTableComponent } from '../diff-table/diff-table.component.js';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto.js';
import { PublishDialogComponent } from '../publish-dialog/publish-dialog.component.js';
import { PublishAction } from '../../interface/publish-action.js';

@Component({
  standalone: true,
  selector: 'app-review-detail',
  imports: [
    FormsModule,
    NgFor,
    DiffTableComponent,
    DatePipe,
    PublishDialogComponent,
    NgIf,
  ],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
  gerritChangeId!: string;

  pseudoCommitDto!: PseudonymGitlabCommitDto;

  fileContents: Map<string, string[]> = new Map();

  assignmentId!: string;

  projectId!: string;

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

    this.reviewSvc
      .getGroupProjectIdByAssignmentId(this.assignmentId)
      .subscribe((projectId) => {
        this.projectId = projectId;
      });
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }

  // open the single parent dialog
  onOpenPublishDialog() {
    this.showPublishDialog = true;
    this.isLeavingPage = false;
  }

  // when the user confirms in that dialog...
  onPublishConfirmed(evt: { action: PublishAction }) {
    this.showPublishDialog = false;

    // 1) gather *all* drafts from every child table
    const allDrafts: GerritCommentInput[] = this.diffTables
      .toArray()
      .flatMap((table) => table.draftComments);

    if (allDrafts.length === 0) {
      console.log('Nothing to publish');
      return;
    }

    // 2) decide NEED_RESOLVE vs APPROVED from evt.action
    const needResolve: boolean = evt.action === 'resolve';

    // 3) call your service once, with the union of every draft
    this.reviewSvc
      .publishReviewerDraftComments(
        this.gerritChangeId,
        this.assignmentId,
        needResolve,
        allDrafts
      )
      .subscribe(() => {
        // 4) tell each child to re-fetch its drafts & published comments
        this.diffTables.forEach((table) => {
          table.fetchDraftComments();
          table.fetchExistedComments();
        });

        if (this.isLeavingPage) {
          this.router.navigate([
            '/review',
            this.pseudoCommitDto.commit.projectId,
          ]);
        }
      });
  }

  getAllDraftCount(): number {
    return this.diffTables
      .toArray()
      .reduce((acc, table) => acc + table.draftComments.length, 0);
  }

  navigateToReviewCommitList() {
    const drafts = this.getAllDraftCount();
    console.log(`Drafts count: ${drafts}`);
    if (drafts > 0) {
      console.log('Drafts exist, opening publish dialog...');
      // Open dialog in “leaving” variant
      this.isLeavingPage = true;
      this.showPublishDialog = true;
    } else {
      // No drafts → go straight back
      this.router.navigate(['/review', this.projectId]);
    }
  }
}
