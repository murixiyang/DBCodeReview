import { Component, Input, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { ReviewService } from '../../http/review.service';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { DatePipe, NgIf } from '@angular/common';
import { ChangeRequestDto } from '../../interface/database/change-request-dto';
import { ReviewAssignmentPseudonymDto } from '../../interface/database/review-assignment-dto';
import { ShortIdPipe } from '../../pipe/short-id.pipe';

@Component({
  selector: 'app-review-commit-list',
  imports: [MatTableModule, MatChipsModule, MatButtonModule, DatePipe, NgIf, ShortIdPipe],
  templateUrl: './review-commit-list.component.html',
  styleUrl: './review-commit-list.component.css',
})
export class ReviewCommitListComponent {
  //   @Input() gerritChangeId!: string;

  @Input() selectedAssignment!: ReviewAssignmentPseudonymDto;

  // Metadata
  projectName!: string;
  authorPseudoName!: string;

  // Table
  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: ChangeRequestDto[] = [];

  constructor(private reviewSvc: ReviewService, private router: Router) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['selectedAssignment'] && this.selectedAssignment) {
      this.getGerritCommitList();
    }
  }

  private getGerritCommitList() {
    this.reviewSvc
      .getChangeRequestForAssignment(this.selectedAssignment.id.toString())
      .subscribe((list) => {
        this.commitList = list;

        console.log('ChangeRequestDto List:', this.commitList);
      });
  }

  /** Navigate to your diff/review screen */
  startReview(item: ChangeRequestDto) {
    this.router.navigate(['/review', item.gerritChangeId]);
  }
}
