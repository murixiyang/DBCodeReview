import { Component, QueryList, ViewChildren } from '@angular/core';
import { DiffTableComponent } from '../../review/diff-table/diff-table.component';
import { ActivatedRoute } from '@angular/router';
import { EvaluationService } from '../../http/evaluation.service';
import { PublishDialogComponent } from '../../review/publish-dialog/publish-dialog.component';
import { DatePipe, NgFor } from '@angular/common';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto';
import { PublishAction } from '../../interface/publish-action';
import { ReviewService } from '../../http/review.service';
import { NamedAuthorCodeDto } from '../../interface/eval/named-author-code-dto';

@Component({
  selector: 'app-eval-review',
  imports: [DiffTableComponent, PublishDialogComponent, NgFor],
  templateUrl: './eval-review.component.html',
  styleUrl: './eval-review.component.css',
})
export class EvalReviewComponent {
  // loaded from the backend
  pseudoCommit!: PseudonymGitlabCommitDto;
  fileKeys: string[] = [];
  fileContents = new Map<string, [string, string]>();

  authorCodeData!: NamedAuthorCodeDto;

  round!: number;
  authorCodeId!: number;
  isAnonymous!: boolean;
  username!: string;
  pseudonym!: string;

  @ViewChildren(DiffTableComponent) diffTables!: QueryList<DiffTableComponent>;

  constructor(
    private evalSvc: EvaluationService,
    private reviewSvc: ReviewService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.round = +this.route.snapshot.paramMap.get('round')!;

    // 1) load the assignment
    this.evalSvc.getEvalReviewAssignment().subscribe((evalReview) => {
      // pick the correct submission and anon‐flag:
      this.authorCodeId =
        this.round === 1 ? evalReview.round1Id : evalReview.round2Id;
      this.isAnonymous =
        this.round === 1
          ? evalReview.round1Anonymous
          : evalReview.round2Anonymous;
      this.pseudonym = evalReview.pseudonym;

      // Load author code detail
      this.evalSvc.getNamedAuthorCode(this.round).subscribe((authorCode) => {
        this.authorCodeData = authorCode;

        // Load file diff
        this.reviewSvc
          .getChangedFileContents(authorCode.gerritChangeId)
          .subscribe((f) => {
            console.log('changed file contents: ', f);

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

  getAllDraftCount(): number {
    // asks each diffTable for its draft count
    // return this.diffTables
    //   .toArray()
    //   .reduce((sum, dt) => sum + dt.getDraftCount(), 0);
    return 0; // Placeholder, replace with actual logic
  }

  onOpenPublishDialog() {
    // this.diffTables.forEach((dt) => dt.prepareToPublish());
  }

  onPublishConfirmed(yes: { action: PublishAction }) {
    // if (!yes) return;
    // // collect all drafts from each diffTable
    // const allDrafts = this.diffTables
    //   .toArray()
    //   .flatMap((dt) => dt.collectDrafts());
    // this.evalSvc.submitReview(allDrafts, this.isAnonymous).subscribe(() => {
    //   alert('Review submitted! Thank you.');
    // });
  }
}
