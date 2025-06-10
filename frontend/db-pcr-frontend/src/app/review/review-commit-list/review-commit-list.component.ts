import { Component, Input, Optional, SimpleChanges } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { ReviewService } from '../../http/review.service';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { DatePipe, NgIf } from '@angular/common';
import { ChangeRequestDto } from '../../interface/database/change-request-dto';
import { ReviewAssignmentPseudonymDto } from '../../interface/database/review-assignment-dto';
import { ShortIdPipe } from '../../pipe/short-id.pipe';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-review-commit-list',
  imports: [
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    DatePipe,
    NgIf,
    ShortIdPipe,
  ],
  templateUrl: './review-commit-list.component.html',
  styleUrl: './review-commit-list.component.css',
})
export class ReviewCommitListComponent {
  @Input() selectedAssignment!: ReviewAssignmentPseudonymDto;
  @Input() isInstructorView = false;

  // Metadata
  projectName!: string;
  authorPseudoName!: string;

  // Table
  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: ChangeRequestDto[] = [];

  constructor(
    private reviewSvc: ReviewService,
    private router: Router,
    @Optional() private dialogRef?: MatDialogRef<ReviewCommitListComponent>
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedAssignment'] && this.selectedAssignment) {
      this.getGerritCommitList();
    }
  }

  private getGerritCommitList() {
    this.reviewSvc
      .getChangeRequestForAssignment(this.selectedAssignment.id.toString())
      .subscribe((list) => {
        this.commitList = list.sort((a, b) => {
          return (
            new Date(b.submittedAt).getTime() -
            new Date(a.submittedAt).getTime()
          );
        });
      });
  }

  /** Navigate to your diff/review screen */
  startReview(item: ChangeRequestDto) {
    const path = this.isInstructorView
      ? ['/maintain/detail', item.gerritChangeId, this.selectedAssignment.id]
      : ['/review/detail', item.gerritChangeId, this.selectedAssignment.id];

    if (this.isInstructorView && this.dialogRef) {
      this.dialogRef.close(); // Close the dialog before navigation
    }

    this.router.navigate(path);
  }
}
