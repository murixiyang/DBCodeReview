import { Component } from '@angular/core';
import { ReviewService } from '../../http/review.service';
import { AuthService } from '../../service/auth.service';
import { AssignmentMetadata } from '../../interface/assignment-metadata';
import { map, Observable } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { AsyncPipe, NgFor, NgIf } from '@angular/common';
import { ReviewCommitListComponent } from '../review-commit-list/review-commit-list.component';

@Component({
  selector: 'app-review-list',
  imports: [NgIf, NgFor, ReviewCommitListComponent],
  templateUrl: './review-list.component.html',
  styleUrl: './review-list.component.css',
})
export class ReviewListComponent {
  projectName!: string;
  username: string | null = null;

  assignmentMetadata!: AssignmentMetadata[];
  selectedAssignment?: AssignmentMetadata;

  constructor(
    private reviewSvc: ReviewService,
    private authSvc: AuthService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectName = this.route.snapshot.paramMap.get('projectName')!;

    // Cache username
    this.authSvc.getUser().subscribe((user) => {
      this.username = user;
      console.log('Username:', this.username);

      this.reviewSvc
        .getAssignmentMetadata(this.username!)
        .pipe(
          map((list) => list.filter((a) => a.projectName === this.projectName))
        )
        .subscribe((list) => {
          this.assignmentMetadata = list;

          if (list.length) {
            this.select(list[0]);
          }
        });
    });
  }

  select(a: AssignmentMetadata) {
    this.selectedAssignment = a;
  }
}
