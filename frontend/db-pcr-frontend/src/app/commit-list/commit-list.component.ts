import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GitlabService } from '../http/gitlab.service';
import { CommitListItem } from '../interface/commit-list-item';

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
  projectId: number = 0;

  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: CommitListItem[] = [];

  constructor(
    private gitLabSvc: GitlabService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.params['projectId'];
    this.getProjectCommits(this.projectId);
  }

  getProjectCommits(projectId: number) {
    this.gitLabSvc.getProjectCommits(projectId).subscribe((data) => {
      console.log('Project commits:', data);
      this.commitList = data.map((commit) => {
        const commitListItem: CommitListItem = {
          status: 'Waiting for Review',
          commit: commit,
        };
        return commitListItem;
      });
    });
  }

  requestReview(listItem: CommitListItem) {
    console.log('Request review for', listItem);
  }
}
