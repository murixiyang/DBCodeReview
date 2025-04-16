import { Component, OnInit } from '@angular/core';
import { ProjectInfoModel } from '../interface/gerrit/project-info';
import { GerritService } from '../http/gerrit.service';
import { Router } from '@angular/router';
import { KeyValuePipe, NgFor, NgIf } from '@angular/common';
import { GitlabCommitInfo } from '../interface/gitlab/gitlab-commit-info';
import { FormsModule } from '@angular/forms';
import { GitlabService } from '../http/gitlab.service';

@Component({
  selector: 'app-project-list',
  imports: [NgIf, NgFor, KeyValuePipe, FormsModule],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  // GitLab Fetch
  repoUrl: string = '';
  repoCommitList: GitlabCommitInfo[] = [];
  error: string = '';

  projectMap: Map<string, ProjectInfoModel> = new Map<
    string,
    ProjectInfoModel
  >();

  constructor(
    private gerritService: GerritService,
    private gitLabService: GitlabService,
    private router: Router
  ) {}

  ngOnInit() {
    this.gerritService
      .getProjectList()
      .subscribe((dataMap: Map<string, ProjectInfoModel>) => {
        this.projectMap = dataMap;
      });
  }

  fetchGitLabCommits() {
    this.gitLabService.getRepoCommits(this.repoUrl).subscribe({
      next: (data) => {
        this.repoCommitList = data;
        this.error = '';
      },
      error: (err) => {
        console.error('Error fetching commits:', err);
        this.error = 'Unable to fetch commits. Please check the url.';
      },
    });
  }

  navigateToChangeList(projectName: string) {
    this.router.navigate(['/commit-list', projectName]);
  }
}
