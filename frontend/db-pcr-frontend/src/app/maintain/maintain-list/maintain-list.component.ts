import { Component, OnInit } from '@angular/core';
import { NgFor, NgIf } from '@angular/common';
import { ProjectDto } from '../../interface/database/project-dto';
import { ProjectService } from '../../http/project.service';
import { ReviewAssignmentUsernameDto } from '../../interface/database/review-assignment-dto copy';
import { MaintainService } from '../../http/maintain.service';
import { FormsModule } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';

@Component({
  selector: 'app-maintain-list',
  imports: [
    NgFor,
    NgIf,
    FormsModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatTableModule,
  ],
  templateUrl: './maintain-list.component.html',
  styleUrl: './maintain-list.component.css',
})
export class MaintainListComponent implements OnInit {
  groupProjects: ProjectDto[] = [];
  selectedProjectId?: number;
  reviewerNum: number = 1;
  reviewAssignments: ReviewAssignmentUsernameDto[] = [];

  // Once assignment is made, cannot change
  locked = false;
  loadingProjects = true;

  constructor(
    private projectSvc: ProjectService,
    private maintainSvc: MaintainService
  ) {}

  ngOnInit() {
    this.loadingProjects = true;
    this.projectSvc.getGroupProjects().subscribe({
      next: (projects) => {
        console.log('Loaded projects:', projects);
        this.groupProjects = projects;
        this.loadingProjects = false;
        if (projects.length) {
          this.selectedProjectId = projects[0].id;
          this.loadAssignments();
        }
      },
      error: (err) => {
        console.error('Failed to load projects', err);
        this.loadingProjects = false;
      },
    });
  }

  onSelectProject(id: number) {
    this.selectedProjectId = id;
    this.loadAssignments();
  }

  loadAssignments() {
    if (!this.selectedProjectId) {
      this.reviewAssignments = [];
      this.locked = false;
      return;
    }
    this.maintainSvc.getAssignedList(this.selectedProjectId).subscribe({
      next: (list) => {
        this.reviewAssignments = list;
        // lock if there are already assignments
        this.locked = this.reviewAssignments.length > 0;
      },
      error: (err) => console.error('Failed to load assignments', err),
    });
  }

  assign() {
    if (!this.selectedProjectId || this.locked) return;
    this.maintainSvc
      .assignReviewers(this.selectedProjectId, this.reviewerNum)
      .subscribe({
        next: (list) => {
          this.reviewAssignments = list;
          // once we’ve generated, lock out further changes
          this.locked = true;
        },
        error: (err) => console.error('Assignment failed', err),
      });
  }
}
