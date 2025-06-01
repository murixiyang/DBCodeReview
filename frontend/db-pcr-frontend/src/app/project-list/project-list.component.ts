import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NgFor } from '@angular/common';
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

    // Fetch group projects to build the database
    this.projectSvc.getGroupProjects().subscribe(() => {});

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
