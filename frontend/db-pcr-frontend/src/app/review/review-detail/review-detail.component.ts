import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../http/review.service.js';
import { GerritCommentInput } from '../../interface/gerrit/gerrit-comment-input.js';
import { FormsModule } from '@angular/forms';
import { GerritCommentInfo } from '../../interface/gerrit/gerrit-comment-info.js';
import { DatePipe, NgFor } from '@angular/common';
import { DiffTableComponent } from '../diff-table/diff-table.component.js';
import { PseudonymGitlabCommitDto } from '../../interface/database/pseudonym-gitlab-commit-dto.js';

@Component({
  standalone: true,
  selector: 'app-review-detail',
  imports: [FormsModule, NgFor, DiffTableComponent, DatePipe],
  templateUrl: './review-detail.component.html',
  styleUrl: './review-detail.component.css',
})
export class ReviewDetailComponent {
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
