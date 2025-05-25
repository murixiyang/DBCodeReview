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

  @ViewChildren(DiffTableComponent)
  diffTables!: QueryList<DiffTableComponent>;

  showPublishDialog = false;

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService,
    private router: Router
  ) {
    const nav = this.router.getCurrentNavigation();

    if (nav?.extras.state && nav.extras.state['assignmentId']) {
      this.assignmentId = nav.extras.state['assignmentId'];
    }
  }

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;

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
  onPublishConfirmed(evt: { action: PublishAction }) {
    this.showPublishDialog = false;

    // 1) gather *all* draft-IDs from every child table
    const allDraftIds = this.diffTables
      .toArray()
      .flatMap((table) => table.draftComments.map((d) => d.id!))
      .filter((id) => !!id);

    if (allDraftIds.length === 0) {
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
        allDraftIds
      )
      .subscribe(() => {
        // 4) tell each child to re-fetch its drafts & published comments
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
