import { DatePipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommitWithStatusDto } from '../../interface/database/commit-with-status-dto';
import { GitlabCommitDto } from '../../interface/database/gitlab-commit-dto';
import { ShortIdPipe } from '../../pipe/short-id.pipe';
import { CommitService } from '../../http/commit.service';
import { ReviewService } from '../../http/review.service';

@Component({
  imports: [
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    DatePipe,
    RouterLink,
    NgIf,
    ShortIdPipe,
  ],
  templateUrl: './commit-list.component.html',
  styleUrl: './commit-list.component.css',
})
export class CommitListComponent implements OnInit {
  projectId: string = '';

  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: CommitWithStatusDto[] = [];

  username: string | null = null;

  constructor(
    private commitSvc: CommitService,
    private reviewSvc: ReviewService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];

    this.commitSvc
      .getCommitsWithStatus(this.projectId)
      .subscribe((gitlabCommits) => {
        this.commitList = gitlabCommits;
      });
  }

  requestReview(commit: CommitWithStatusDto) {
    this.reviewSvc
      .postRequestReview(this.projectId, commit.commit.gitlabCommitId)
      .subscribe({
        next: (response) => {
          // Update the review status in the database
          this.commitSvc
            .getCommitsWithStatus(this.projectId)
            .subscribe((gitlabCommits) => {
              this.commitList = gitlabCommits;
            });
        },
        error: (error) => {
          console.error('Error requesting review:', error);
        },
      });
  }

  checkReviewPage(commit: CommitWithStatusDto) {
    // Find assignmentId
    this.reviewSvc
      .getAuthorAssignmentPseudonymDtoList(this.projectId)
      .subscribe((pseudonymDtos) => {
        this.commitSvc
          .getGerritChangeIdByCommitId(commit.commit.id.toString())
          .subscribe((gerritChangeId) => {
            this.router.navigate(['/author/detail', gerritChangeId], {
              state: {
                // As author, can use any assignmentId related to them
                assignmentId: pseudonymDtos[0].id,
              },
            });
          });
      });
  }
}
