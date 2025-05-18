import { Component } from '@angular/core';
import { ReviewService } from '../../http/review.service';
import { ActivatedRoute } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { ReviewCommitListComponent } from '../review-commit-list/review-commit-list.component';
import { ChangeRequestDto } from '../../interface/database/change-request-dto';
import { ReviewAssignmentPseudonymDto } from '../../interface/database/review-assignment-dto';

@Component({
  selector: 'app-review-list',
  imports: [NgIf, NgFor, ReviewCommitListComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css',
})
export class ReviewListComponent {
  groupProjectId!: string;
  projectName!: string;

  reviewAssignments!: ReviewAssignmentPseudonymDto[];
  selectedAssignment?: ReviewAssignmentPseudonymDto;

  constructor(
    private reviewSvc: ReviewService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.groupProjectId = this.route.snapshot.paramMap.get('projectId')!;

    this.reviewSvc
      .getReviewAssignmentPseudonymDtoList(this.groupProjectId!)
      .subscribe((data) => {
        this.reviewAssignments = data;

        this.selectedAssignment = this.reviewAssignments[0];
        this.projectName = this.selectedAssignment.groupProjectName;
      });
  }

  select(assignment: ReviewAssignmentPseudonymDto) {
    this.selectedAssignment = assignment;
  }
}
