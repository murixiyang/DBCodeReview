import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { GitlabService } from '../http/gitlab.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';
import { MaintainService } from '../http/maintain.service';
import { AuthService } from '../service/auth.service';

@Component({
  imports: [NgFor, AsyncPipe],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projects$!: Observable<ProjectSchema[]>;
  projectsToReview$!: Observable<ProjectSchema[]>;

  username: string | null = null;

  constructor(
    private gitLabService: GitlabService,
    private maintainSvc: MaintainService,
    private authSvc: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.projects$ = this.gitLabService.getProjects();

    this.authSvc.getUser().subscribe((user) => {
      this.username = user;

      this.projectsToReview$ = this.maintainSvc.getProjectsToReview(
        this.username!
      );
    });
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }
}
