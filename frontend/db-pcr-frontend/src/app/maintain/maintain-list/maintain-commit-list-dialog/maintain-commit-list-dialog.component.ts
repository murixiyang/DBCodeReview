import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
} from '@angular/material/dialog';
import { ReviewCommitListComponent } from '../../../review/review-commit-list/review-commit-list.component';
import { ReviewAssignmentPseudonymDto } from '../../../interface/database/review-assignment-dto';

@Component({
  selector: 'app-maintain-commit-list-dialog',
  imports: [ReviewCommitListComponent, MatDialogModule],
  templateUrl: './maintain-commit-list-dialog.component.html',
  styleUrl: './maintain-commit-list-dialog.component.css',
})
export class MaintainCommitListDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { selectedAssignment: ReviewAssignmentPseudonymDto }
  ) {}
}
