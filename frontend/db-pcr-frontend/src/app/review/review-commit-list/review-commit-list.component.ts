import { Component, Input, SimpleChanges } from '@angular/core';
import { ChangeInfo } from '../../interface/gerrit/change-info';
import { Router } from '@angular/router';
import { MaintainService } from '../../http/maintain.service';
import { GerritService } from '../../http/gerrit.service';
import { DatabaseService } from '../../http/database.service';
import { AuthService } from '../../service/auth.service';
import { ReviewService } from '../../http/review.service';
import { tap } from 'rxjs';
import { GerritChangeListItem } from '../../interface/commit-list-item';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { DatePipe, NgIf } from '@angular/common';

@Component({
  selector: 'app-review-commit-list',
  imports: [MatTableModule, MatChipsModule, MatButtonModule, DatePipe, NgIf],
  templateUrl: './review-commit-list.component.html',
  styleUrl: './review-commit-list.component.css',
})
export class ReviewCommitListComponent {
  @Input() assignmentUuid!: string;

  // Metadata
  projectName!: string;
  authorPseudoName!: string;

  // Table
  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: GerritChangeListItem[] = [];

  private authorName!: string;
  private username!: string;

  constructor(
    private reviewSvc: ReviewService,
    private gerritSvc: GerritService,
    private databaseSvc: DatabaseService,
    private authSvc: AuthService,
    private router: Router
  ) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['assignmentUuid'] && this.assignmentUuid) {
      this.getGerritCommitList();
    }
  }

  private getAssignmentMeta() {
    this.reviewSvc
      .getAssignmentMetaByUuid(this.assignmentUuid)
      .subscribe((meta) => {
        this.projectName = meta.projectName;
        this.authorPseudoName = meta.authorPseudoName;
      });
  }

  private getGerritCommitList() {
    this.reviewSvc
      .getGerritChangeInfoByUuid(this.assignmentUuid)
      .subscribe((list) => {
        console.log('Gerrit ChangeInfo List:', list);
        this.commitList = list.map((change) => {
          return {
            status: 'WAITING_FOR_REPLY',
            change: change,
          } as GerritChangeListItem;
        });
      });
  }

  /** Navigate to your diff/review screen */
  startReview(item: GerritChangeListItem) {
    this.router.navigate([
      '/review',
      this.assignmentUuid,
      item.change.changeId,
    ]);
  }
}
