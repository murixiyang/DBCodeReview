import { Component, OnInit } from '@angular/core';
import { ProjectInfoModel } from '../interface/gerrit/project-info';
import { ChangeInfo } from '../interface/gerrit/change-info';
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

  projectMap: Map<string, ProjectInfoModel> = new Map<
    string,
    ProjectInfoModel
  >();
  selectedProject: string = '';

  changeList: ChangeInfo[] = [];

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

  onSelectProject(projectName: string) {
    this.selectedProject = projectName;
    this.getChangesOfProject(projectName);
  }

  getChangesOfProject(projectName: string) {
    this.gerritService
      .getChangesOfProject(projectName)
      .subscribe((data: ChangeInfo[]) => {
        this.changeList = data;
      });
  }

  fetchGitLabCommits() {
    this.gitLabService
      .getRepoCommits(this.repoUrl)
      .subscribe((data: GitlabCommitInfo[]) => {
        this.repoCommitList = data;
      });
  }

  navigateToChangeDetails(changeId: string) {
    this.router.navigate(['/change-detail', changeId]);
  }
}
