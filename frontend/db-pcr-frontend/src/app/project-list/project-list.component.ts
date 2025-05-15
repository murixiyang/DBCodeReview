import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { GitlabService } from '../http/gitlab.service';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';
import { MaintainService } from '../http/maintain.service';
import { AuthService } from '../service/auth.service';
import { GerritService } from '../http/gerrit.service';
import { ReviewService } from '../http/review.service';
import { ProjectDto } from '../interface/database/project-dto';

@Component({
  imports: [NgFor, AsyncPipe],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projects$!: Observable<ProjectDto[]>;
  projectsToReview$!: Observable<ProjectDto[]>;

  username: string | null = null;

  constructor(
    private gitLabSvc: GitlabService,
    private reviewSvc: ReviewService,
    private authSvc: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.projects$ = this.gitLabSvc.getProjects();

    this.authSvc.getUser().subscribe((user) => {
      this.username = user;

      this.projectsToReview$ = this.reviewSvc.getProjectsToReview(
        this.username!
      );
    });
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }

  navigateToReviewList(projectId: number) {
    this.router.navigate(['/review', projectId]);
  }
}
