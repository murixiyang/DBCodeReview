import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { Observable } from 'rxjs';
import { ProjectSchema } from '@gitbeaker/rest';
import { MaintainService } from '../http/maintain.service';
import { AuthService } from '../service/auth.service';
import { GerritService } from '../http/gerrit.service';
import { ReviewService } from '../http/review.service';
import { ProjectDto } from '../interface/database/project-dto';
import { ProjectService } from '../http/project.service';

@Component({
  imports: [NgFor],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projects!: ProjectDto[];
  projectsToReview!: ProjectDto[];

  constructor(
    private projectSvc: ProjectService,
    private reviewSvc: ReviewService,
    private router: Router
  ) {}

  ngOnInit() {
    this.projectSvc.getProjects().subscribe((projects) => {
      this.projects = projects;
    });

    this.reviewSvc.getProjectsToReview().subscribe((projects) => {
      this.projectsToReview = projects;
    });
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }

  navigateToReviewList(projectId: number) {
    this.router.navigate(['/review', projectId]);
  }
}
