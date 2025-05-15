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
  projects!: ProjectDto[];
  projectsToReview!: ProjectDto[];

  constructor(
    private gitLabSvc: GitlabService,
    private reviewSvc: ReviewService,
    private router: Router
  ) {}

  ngOnInit() {
    this.gitLabSvc.getProjects().subscribe((projects) => {
      this.projects = projects;
      console.log('Projects:', this.projects);
    });

    this.reviewSvc.getProjectsToReview().subscribe((projects) => {
      this.projectsToReview = projects;
      console.log('Projects to review:', this.projectsToReview);
    });
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }

  navigateToReviewList(projectId: number) {
    this.router.navigate(['/review', projectId]);
  }
}
