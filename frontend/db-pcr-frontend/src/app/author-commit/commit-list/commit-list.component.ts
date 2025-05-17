import { DatePipe, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GitlabService } from '../../http/gitlab.service';
import { GerritService } from '../../http/gerrit.service';
import { DatabaseService } from '../../http/database.service';
import { AuthService } from '../../service/auth.service';
import { CommitWithStatusDto } from '../../interface/database/commit-with-status-dto';
import { GitlabCommitDto } from '../../interface/database/gitlab-commit-dto';
import { ShortIdPipe } from '../../pipe/short-id.pipe';

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
    private gitLabSvc: GitlabService,
    private gerritSvc: GerritService,
    private databaseSvc: DatabaseService,
    private authSvc: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];

    this.gitLabSvc
      .getCommitsWithStatus(this.projectId)
      .subscribe((gitlabCommits) => {
        this.commitList = gitlabCommits;
      });
  }

  getProjectCommitsWithStatus(projectId: string) {}

  requestReview(commit: GitlabCommitDto) {
    this.gerritSvc
      .postRequestReview(this.projectId, commit.gitlabCommitId)
      .subscribe({
        next: (response) => {
          console.log('Request review response:', response);

          // Update the review status in the database
        },
        error: (error) => {
          console.error('Error requesting review:', error);
        },
      });
  }

  revertSubmission(commit: GitlabCommitDto) {
    // Update the review status in the database
  }
}
