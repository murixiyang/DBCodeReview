import { Component, QueryList, ViewChildren } from '@angular/core';
import { VersionSelectorComponent } from '../../../review/version-selector/version-selector.component';
import { DiffTableComponent } from '../../../review/diff-table/diff-table.component';
import { PseudonymGitlabCommitDto } from '../../../interface/database/pseudonym-gitlab-commit-dto';
import { ActivatedRoute, Router } from '@angular/router';
import { ReviewService } from '../../../http/review.service';
import { DatePipe, NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-maintain-detail',
  imports: [
    VersionSelectorComponent,
    DiffTableComponent,
    NgIf,
    DatePipe,
    NgFor,
  ],
  templateUrl: './maintain-detail.component.html',
  styleUrl: './maintain-detail.component.css',
})
export class MaintainDetailComponent {
  gerritChangeId!: string;

  pseudoCommitDto!: PseudonymGitlabCommitDto;

  fileContents: Map<string, string[]> = new Map();

  assignmentId!: string;

  @ViewChildren(DiffTableComponent)
  diffTables!: QueryList<DiffTableComponent>;

  showPublishDialog = false;
  isLeavingPage = false;

  constructor(
    private route: ActivatedRoute,
    private reviewSvc: ReviewService,
    private router: Router
  ) {}

  ngOnInit() {
    this.gerritChangeId = this.route.snapshot.paramMap.get('gerritChangeId')!;
    this.assignmentId = this.route.snapshot.paramMap.get('assignmentId')!;

    // view selector will fire output to fetch file contents

    this.reviewSvc
      .getAuthorPseudonymCommit(this.gerritChangeId)
      .subscribe((p) => {
        this.pseudoCommitDto = p;
      });
  }

  get fileKeys(): string[] {
    return Array.from(this.fileContents.keys());
  }

  onVersionSelected(previousChangeId: string) {
    this.reviewSvc
      .getChangedFileContentsCompareTo(this.gerritChangeId, previousChangeId)
      .subscribe((f) => {
        this.fileContents = new Map(Object.entries(f));
      });
  }

  navigateToMaintainerPage() {
    this.router.navigate(['/maintain/project-list']);
  }
}
