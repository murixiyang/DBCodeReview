import { Component, Input } from '@angular/core';
import { ChangeInfo } from '../../interface/gerrit/change-info';

@Component({
  selector: 'app-review-commit-list',
  imports: [],
  templateUrl: './review-commit-list.component.html',
  styleUrl: './review-commit-list.component.css',
})
export class ReviewCommitListComponent {
  @Input() assignmentUuid!: string;

  // Metadata
  projectName!: string;
  authorPseudonym!: string;

  // Table
  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  commitList: ChangeInfo[] = [];

  private authorName!: string;
  private username!: string;
}
