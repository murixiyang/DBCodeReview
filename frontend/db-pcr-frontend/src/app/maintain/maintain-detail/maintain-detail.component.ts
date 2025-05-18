import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MaintainService } from '../../http/maintain.service';
import { ProjectDto } from '../../interface/database/project-dto';
import { ReviewAssignmentUsernameDto } from '../../interface/database/review-assignment-dto copy';
import { ProjectService } from '../../http/project.service';

@Component({
  selector: 'app-maintain-detail',
  imports: [FormsModule, NgIf, NgFor],
  templateUrl: './maintain-detail.component.html',
  styleUrl: './maintain-detail.component.css',
})
export class MaintainDetailComponent implements OnInit {
  groupProjects: ProjectDto[] = [];
  groupGitlabProjectId!: number;
  reviewerNum: number = 2;
  reviewAssignments: ReviewAssignmentUsernameDto[] = [];

  constructor(
    private projectSvc: ProjectService,
    private maintainSvc: MaintainService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectSvc.getGroupProjects().subscribe({
      next: (ps) => {
        this.groupProjects = ps;

        if (ps.length) {
          this.groupGitlabProjectId = ps[0].gitlabProjectId;

          this.showAssigned();
        }
      },
      error: (err) => console.error('Failed to load projects', err),
    });
  }

  assign(): void {
    if (!this.groupGitlabProjectId) return;

    this.maintainSvc
      .assignReviewers(this.groupGitlabProjectId, this.reviewerNum)
      .subscribe({
        next: (assignments) => (this.reviewAssignments = assignments),
        error: (err) => console.error('Assignment failed', err),
      });
  }

  showAssigned(): void {
    if (!this.groupGitlabProjectId) return;

    this.maintainSvc.getAssignedList(this.groupGitlabProjectId).subscribe({
      next: (assignments) => (this.reviewAssignments = assignments),
      error: (err) => console.error('Failed to load reviewers', err),
    });
  }
}
