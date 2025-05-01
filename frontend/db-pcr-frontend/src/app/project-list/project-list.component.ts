import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { GitlabService } from '../http/gitlab.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';

@Component({
  imports: [NgFor, AsyncPipe],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projects$!: Observable<ProjectSchema[]>;

  constructor(private gitLabService: GitlabService, private router: Router) {}

  ngOnInit() {
    this.projects$ = this.gitLabService.getProjects();
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }
}
