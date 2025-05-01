import { Component, OnInit } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { ProjectInfoModel } from '../interface/gerrit/project-info';
import { GerritService } from '../http/gerrit.service';
import { Router } from '@angular/router';
import { AsyncPipe, KeyValuePipe, NgFor, NgIf } from '@angular/common';
import { GitlabCommitInfo } from '../interface/gitlab/gitlab-commit-info';
import { FormsModule } from '@angular/forms';
import { GitlabService } from '../http/gitlab.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';

@Component({
  // Removed invalid imports property
  imports: [NgFor, FormsModule, MatListModule, AsyncPipe],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projects$!: Observable<ProjectSchema[]>;

  constructor(private gitLabService: GitlabService, private router: Router) {}

  ngOnInit() {
    this.projects$ = this.gitLabService.getProjects();
  }

  // fetchGitLabCommits() {
  //   this.gitLabService.getRepoCommits(this.repoUrl).subscribe({
  //     next: (data) => {
  //       this.repoCommitList = data;
  //       this.error = '';
  //     },
  //     error: (err) => {
  //       console.error('Error fetching commits:', err);
  //       this.error = 'Unable to fetch commits. Please check the url.';
  //     },
  //   });
  // }

  navigateToCommitList(projectName: string) {
    this.router.navigate(['/commit-list', projectName]);
  }
}
