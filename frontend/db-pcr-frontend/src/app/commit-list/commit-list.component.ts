import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GitlabService } from '../http/gitlab.service';
import { CommitListItem } from '../interface/commit-list-item';
import { GerritService } from '../http/gerrit.service';
import { DatabaseService } from '../http/database.service';
import { AuthService } from '../service/auth.service';

@Component({
  imports: [
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    DatePipe,
    RouterLink,
    NgIf
  ],
  templateUrl: './commit-list.component.html',
  styleUrl: './commit-list.component.css',
})
export class CommitListComponent implements OnInit {
  projectId: string = '';

  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: CommitListItem[] = [];

  username: string | null = null;

  constructor(
    private gitLabSvc: GitlabService,
    private gerritSvc: GerritService,
    private databaseSvc: DatabaseService,
    private authSvc: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];

    // Cache username
    this.authSvc.getUser().subscribe((user) => {
      this.username = user;
      console.log('Username:', this.username);

      this.getProjectCommits(this.projectId);
    });
  }

  getProjectCommits(projectId: string) {
    if (!this.username) {
      console.error('User not authenticated');
      return;
    }

    // Not null username
    const username = this.username;

    this.gitLabSvc.getProjectCommits(projectId).subscribe((gitlabCommits) => {
      this.databaseSvc
        .getReviewStatus(username, projectId)
        .subscribe((reviewStatusEntityList) => {
          this.commitList = gitlabCommits.map((commit) => {
            const existingStatus = reviewStatusEntityList.filter(
              (status) => status.commitSha === commit.id
            )[0]?.reviewStatus;

            const commitListItem: CommitListItem = {
              status: existingStatus ? existingStatus : 'NOT_SUBMITTED',
              commit: commit,
            };

            // If commit not in DB, store it
            if (!existingStatus) {
              this.databaseSvc
                .createReviewStatus(
                  username,
                  projectId,
                  commit.id,
                  'NOT_SUBMITTED'
                )
                .subscribe();
            }

            return commitListItem;
          });
        });
    });
  }

  requestReview(listItem: CommitListItem) {
    if (!this.username) {
      console.error('User not authenticated');
      return;
    }

    const username = this.username;

    this.gerritSvc
      .postRequestReview(this.projectId, listItem.commit.id)
      .subscribe({
        next: (response) => {
          console.log('Request review response:', response);
          listItem.status = 'WAITING_FOR_REVIEW';

          // Update the review status in the database
          this.databaseSvc
            .updateReviewStatus(
              username,
              this.projectId,
              listItem.commit.id,
              'WAITING_FOR_REVIEW'
            )
            .subscribe({
              next: (updateResponse) => {
                console.log('Review status updated:', updateResponse);
              },
              error: (error) => {
                console.error('Error updating review status:', error);
              },
            });
        },
        error: (error) => {
          console.error('Error requesting review:', error);
        },
      });
  }

  revertSubmission(listItem: CommitListItem) {
    if (!this.username) {
      console.error('User not authenticated');
      return;
    }

    const username = this.username;

    this.gerritSvc
      .postRequestReview(this.projectId, listItem.commit.id)
      .subscribe({
        next: (response) => {
          console.log('Request review response:', response);
          listItem.status = 'NOT_SUBMITTED'

          // Update the review status in the database
          this.databaseSvc
            .updateReviewStatus(
              username,
              this.projectId,
              listItem.commit.id,
              'NOT_SUBMITTED'
            )
            .subscribe({
              next: (updateResponse) => {
                console.log('Review status updated:', updateResponse);
              },
              error: (error) => {
                console.error('Error updating review status:', error);
              },
            });
        },
        error: (error) => {
          console.error('Error requesting review:', error);
        },
      });
  }
}
