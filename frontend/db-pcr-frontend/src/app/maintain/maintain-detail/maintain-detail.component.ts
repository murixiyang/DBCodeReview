import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ProjectSchema } from '@gitbeaker/rest';
import { ReviewAssignment } from '../../interface/ReviewAssignment';
import { MaintainService } from '../../http/maintain.service';
import { GitlabService } from '../../http/gitlab.service';

@Component({
  selector: 'app-maintain-detail',
  imports: [FormsModule, NgIf, NgFor],
  templateUrl: './maintain-detail.component.html',
  styleUrl: './maintain-detail.component.css',
})
export class MaintainDetailComponent implements OnInit {
  projects: ProjectSchema[] = [];
  projectId!: number;
  reviewerNum: number = 1;
  reviewAssignments: ReviewAssignment[] = [];

  constructor(
    private gitlabSvc: GitlabService,
    private maintainSvc: MaintainService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.gitlabSvc.getGroupProjects().subscribe({
      next: (ps) => {
        this.projects = ps;

        console.log('Projects:', ps);

        if (ps.length) {
          this.projectId = ps[0].id;

          console.log(ps[0]);
          console.log(ps[0].name_with_namespace);
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
}
