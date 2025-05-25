import { Component } from '@angular/core';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../http/review.service';
import { AuthorDiffTableComponent } from '../author-diff-table/author-diff-table.component';
import { DatePipe, NgFor } from '@angular/common';

@Component({
  selector: 'app-author-review-detail',
  imports: [AuthorDiffTableComponent, DatePipe, NgFor],
  templateUrl: './author-review-detail.component.html',
  styleUrl: './author-review-detail.component.css',
})
export class AuthorReviewDetailComponent {
  gerritChangeId!: string;

  pseudoCommitDto!: PseudonymGitlabCommitDto;

  fileContents: Map<string, string[]> = new Map();

  assignmentId!: string;

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
}
