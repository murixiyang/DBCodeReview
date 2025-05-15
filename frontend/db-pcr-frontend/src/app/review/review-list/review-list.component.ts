import { Component } from '@angular/core';
import { ReviewService } from '../../http/review.service';
import { AuthService } from '../../service/auth.service';
import { AssignmentMetadata } from '../../interface/assignment-metadata';
import { map, Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { ReviewCommitListComponent } from '../review-commit-list/review-commit-list.component';
import { ChangeRequestDto } from '../../interface/database/change-request-dto';
import { ProjectUserPseudonymDto } from '../../interface/database/project-user-pseudonym-dto';
import { ReviewAssignmentUsernameDto } from '../../interface/database/review-assignment-dto copy';
import { ReviewAssignmentPseudonymDto } from '../../interface/database/review-assignment-dto';
import { ChangeStatus } from '../../interface/change-status';

@Component({
  selector: 'app-review-list',
  imports: [NgIf, NgFor, ReviewCommitListComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css',
})
export class ReviewListComponent {
  projectId!: string;

  changeRequests!: ChangeRequestDto[];
  selectedChangeRequest?: ChangeRequestDto;

  constructor(
    private reviewSvc: ReviewService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectId = this.route.snapshot.paramMap.get('projectId')!;

    this.reviewSvc
      .getChangeRequestForProject(this.projectId!)
      .subscribe((data) => {
        this.changeRequests = data;
        console.log('Change Requests:', this.changeRequests);

        this.selectedChangeRequest = this.changeRequests[0];
      });
  }

  select(c: ChangeRequestDto) {
    this.selectedChangeRequest = c;
  }
}
