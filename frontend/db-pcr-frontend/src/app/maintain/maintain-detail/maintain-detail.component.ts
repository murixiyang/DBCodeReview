import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ProjectSchema } from '@gitbeaker/rest';
import { ReviewAssignment } from '../../interface/review-assignment';
import { MaintainService } from '../../http/maintain.service';
import { GitlabService } from '../../http/gitlab.service';
import { ProjectDto } from '../../interface/database/project-dto';
import { ReviewAssignmentPseudonymDto } from '../../interface/database/review-assignment-dto';
import { ReviewAssignmentUsernameDto } from '../../interface/database/review-assignment-dto copy';

@Component({
  selector: 'app-maintain-detail',
  imports: [FormsModule, NgIf, NgFor],
  templateUrl: './maintain-detail.component.html',
  styleUrl: './maintain-detail.component.css',
})
export class MaintainDetailComponent implements OnInit {
  projects: ProjectDto[] = [];
  projectId!: number;
  reviewerNum: number = 2;
  reviewAssignments: ReviewAssignmentUsernameDto[] = [];

  constructor(
    private gitlabSvc: GitlabService,
    private maintainSvc: MaintainService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.gitlabSvc.getGroupProjects().subscribe({
      next: (ps) => {
        this.projects = ps;

        if (ps.length) {
          this.projectId = ps[0].id;

          this.showAssigned();
        }
      },
      error: (err) => console.error('Failed to load projects', err),
    });
  }

  assign(): void {
    if (!this.projectId) return;

    this.maintainSvc
      .assignReviewers(this.projectId, this.reviewerNum)
      .subscribe({
        next: (assignments) => (this.reviewAssignments = assignments),
        error: (err) => console.error('Assignment failed', err),
      });
  }

  showAssigned(): void {
    if (!this.projectId) return;

    this.maintainSvc.getAssignedList(this.projectId).subscribe({
      next: (assignments) => (this.reviewAssignments = assignments),
      error: (err) => console.error('Failed to load reviewers', err),
    });
  }
}
