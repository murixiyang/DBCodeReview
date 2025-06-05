import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChangeRequestDto } from '../../interface/database/change-request-dto';
import { ReviewService } from '../../http/review.service';
import { DatePipe, NgFor, NgIf } from '@angular/common';
import { ShortIdPipe } from '../../pipe/short-id.pipe';

interface DisplayChangeRequest extends ChangeRequestDto {
  versionNumber: number;
}

@Component({
  selector: 'app-version-selector',
  imports: [FormsModule, DatePipe, NgIf, NgFor],
  templateUrl: './version-selector.component.html',
  styleUrl: './version-selector.component.css',
})
export class VersionSelectorComponent {
  @Input() assignmentId!: string;
  @Input() gerritChangeId!: string;

  @Output() versionSelected = new EventEmitter<string>();

  previousChangeRequests: DisplayChangeRequest[] = [];
  selectedPreviousChangeId!: string;

  constructor(private reviewSvc: ReviewService) {}

  ngOnInit(): void {
    this.reviewSvc
      .getChangeRequestForAssignment(this.assignmentId)
      .subscribe((changeRequests) => {
        const sorted = [...changeRequests].sort(
          (a, b) =>
            new Date(a.submittedAt).getTime() -
            new Date(b.submittedAt).getTime()
        );

        console.log('sorted change requests:', sorted);

        const current = sorted.find(
          (cr) => cr.gerritChangeId === this.gerritChangeId
        );
        if (!current) return;

        this.previousChangeRequests = sorted
          .filter(
            (cr) => new Date(cr.submittedAt) < new Date(current.submittedAt)
          )
          .map((cr, i) => ({ ...cr, versionNumber: i + 1 }));

        if (this.previousChangeRequests.length > 0) {
          this.selectedPreviousChangeId =
            this.previousChangeRequests[0].gerritChangeId;
          this.versionSelected.emit(this.selectedPreviousChangeId);
        } else {
          this.versionSelected.emit('');
        }
      });
  }

  onVersionSelected(): void {
    if (this.selectedPreviousChangeId) {
      this.versionSelected.emit(this.selectedPreviousChangeId);
    }
  }
}
