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
  ],
  templateUrl: './commit-list.component.html',
  styleUrl: './commit-list.component.css',
})
export class CommitListComponent implements OnInit {
  projectId: string = '';

  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: CommitListItem[] = [];

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
    this.getProjectCommits(this.projectId);
  }

  // getProjectCommits(projectId: string) {
  //   this.gitLabSvc.getProjectCommits(projectId).subscribe((data) => {
  //     console.log('Project commits:', data);
  //     this.commitList = data.map((commit) => {
  //       const commitListItem: CommitListItem = {
  //         status: 'Not Submitted',
  //         commit: commit,
  //       };
  //       return commitListItem;
  //     });
  //   });
  // }

  getProjectCommits(projectId: string) {
    this.authSvc.getUser().subscribe((user) => {
      const username = user;

      if (!username) {
        console.error('User not authenticated');
        return;
      }

      this.gitLabSvc.getProjectCommits(projectId).subscribe((gitlabCommits) => {
        this.databaseSvc
          .getReviewStatus(username, projectId)
          .subscribe((reviewStatusEntityList) => {
            this.commitList = gitlabCommits.map((commit) => {

              const existingStatus = reviewStatusEntityList.filter(
                (status) => status.commitSha === commit.id
              )[0]?.reviewStatus;

              const commitListItem: CommitListItem = {
                status: existingStatus ? existingStatus : 'Not Submitted',
                commit: commit,
              };

              // If commit not in DB, store it
              if (!existingStatus) {
                this.databaseSvc
                  .getReviewStatus(projectId, commit.id)
                  .subscribe();
              }

              return commitListItem;
            });
          });
      });
    });
  }

  requestReview(listItem: CommitListItem) {
    console.log('Requesting review for commit:', listItem);
    this.gerritSvc
      .postRequestReview(this.projectId, listItem.commit.id)
      .subscribe({
        next: (response) => {
          console.log('Request review response:', response);
          listItem.status = 'Waiting for Review';
        },
        error: (error) => {
          console.error('Error requesting review:', error);
        },
      });
  }
}
