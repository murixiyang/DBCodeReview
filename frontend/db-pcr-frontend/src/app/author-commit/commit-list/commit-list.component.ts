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
import { finalize } from 'rxjs';

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

  // This flag is true whenever any requestReview() call is still pending.
  isRequestInFlight = false;

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
        this.commitList = gitlabCommits.sort((a, b) => {
          return (
            new Date(b.commit.committedAt).getTime() -
            new Date(a.commit.committedAt).getTime()
          );
        });
      });
  }

  requestReview(commit: CommitWithStatusDto) {
    // If some other row is already in flight, do nothing.
    if (this.isRequestInFlight) {
      return;
    }

    this.isRequestInFlight = true;

    this.reviewSvc
      .postRequestReview(this.projectId, commit.commit.gitlabCommitId)
      .pipe(
        finalize(() => {
          this.isRequestInFlight = false;
        })
      )
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
          // (the finalize() above will flip isRequestInFlight back to false)
        },
      });
  }

  checkReviewPage(commit: CommitWithStatusDto) {
    if (this.isRequestInFlight) {
      return;
    }

    // Find assignmentId
    this.reviewSvc
      .getAuthorAssignmentPseudonymDtoList(this.projectId)
      .subscribe((pseudonymDtos) => {
        this.commitSvc
          .getGerritChangeIdByCommitId(commit.commit.id.toString())
          .subscribe((gerritChangeId) => {
            this.router.navigate([
              '/author/detail',
              gerritChangeId,
              pseudonymDtos[0].id,
            ]);
          });
      });
  }
}
