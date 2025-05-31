import { Component, QueryList, ViewChildren } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EvaluationService } from '../../http/evaluation.service';
import { NgFor, NgIf } from '@angular/common';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto';
import { ReviewService } from '../../http/review.service';
import { NamedAuthorCodeDto } from '../../interface/eval/named-author-code-dto';
import { EvalTableComponent } from '../eval-table/eval-table.component';
import { AuthService } from '../../service/auth.service';
import { AuthorPublishDialogComponent } from '../../author-commit/author-publish-dialog/author-publish-dialog.component';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input';

@Component({
  selector: 'app-eval-review',
  imports: [AuthorPublishDialogComponent, NgFor, EvalTableComponent, NgIf],
  templateUrl: './eval-review.component.html',
  styleUrl: './eval-review.component.css',
})
export class EvalReviewComponent {
  // loaded from the backend
  pseudoCommit!: PseudonymGitlabCommitDto;
  fileContents = new Map<string, [string, string]>();

  authorCodeData!: NamedAuthorCodeDto;

  round!: number;
  authorCodeId!: number;
  isAnonymous!: boolean;
  username!: string;
  commenterDisplayName!: string;

  showPublishDialog = false;

  @ViewChildren(EvalTableComponent) diffTables!: QueryList<EvalTableComponent>;

  constructor(
    private evalSvc: EvaluationService,
    private reviewSvc: ReviewService,
    private authSvc: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // listen for any change to the "round" param
    this.route.paramMap.subscribe((params) => {
      this.round = +params.get('round')!;

      // now re-run all of your data-loading logic
      this.loadAssignment();
    });

    // (optional) load the current user once
    this.authSvc.getUser().subscribe((username) => {
      this.username = username!;
    });
  }

  private loadAssignment() {
    this.evalSvc.getEvalReviewAssignment().subscribe((evalReview) => {
      this.authorCodeId =
        this.round === 1 ? evalReview.round1Id : evalReview.round2Id;
      this.isAnonymous =
        this.round === 1
          ? evalReview.round1Anonymous
          : evalReview.round2Anonymous;
      this.commenterDisplayName = this.isAnonymous
        ? evalReview.pseudonym
        : this.username;

      this.evalSvc.getNamedAuthorCode(this.round).subscribe((authorCode) => {
        this.authorCodeData = authorCode;
        this.reviewSvc
          .getChangedFileContents(authorCode.gerritChangeId)
          .subscribe((f) => {
            this.fileContents = new Map(Object.entries(f));
          });
      });
    });
  }

  getHeaderLabel() {
    if (this.isAnonymous) {
      return `You are reviewing anonymously`;
    } else {
      return `You are signed in as ${this.username}`;
    }
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }

  getAllDraftCount(): number {
    return this.diffTables
      .toArray()
      .reduce((acc, table) => acc + table.draftComments.length, 0);
  }

  // open the single parent dialog
  onOpenPublishDialog() {
    this.showPublishDialog = true;
  }

  onPublishConfirmed() {
    this.showPublishDialog = false;

    // gather *all* drafts from every child table
    const allDrafts: GerritCommentInput[] = this.diffTables
      .toArray()
      .flatMap((table) => table.draftComments);

    // call your service once, with the union of every draft
    this.evalSvc
      .publishDraftComments(this.authorCodeData.gerritChangeId, allDrafts)
      .subscribe(() => {
        // Go to next round
        if (this.round === 1) {
          this.router.navigate(['/eval/review', 2]);
        } else {
          this.router.navigate(['/eval/survey']);
        }
      });
  }
}
