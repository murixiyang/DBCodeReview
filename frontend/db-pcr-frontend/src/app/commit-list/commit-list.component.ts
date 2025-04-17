import { DatePipe, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { ChangeInfo } from '../interface/gerrit/change-info';
import { ActivatedRoute, Router } from '@angular/router';
import { GerritService } from '../http/gerrit.service';
import { ReviewEntry } from '../interface/review-status';

@Component({
  imports: [
    MatTableModule,
    MatChipsModule,
    MatButtonModule,
    DatePipe,
  ],
  templateUrl: './commit-list.component.html',
  styleUrl: './commit-list.component.css',
})
export class CommitListComponent implements OnInit {
  projectName: string = '';
  commitList: ChangeInfo[] = [];

  displayedColumns = ['status', 'hash', 'message', 'date', 'action'];
  dataSource: ReviewEntry[] = [
    {
      status: 'Waiting for Review',
      hash: 'abc123',
      message: 'Initial commit',
      date: new Date('2023-10-01'),
    },
  ];

  constructor(
    private gerritService: GerritService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.projectName = this.route.snapshot.params['project-name'];
    this.getChangesOfProject(this.projectName);
  }

  getChangesOfProject(projectName: string) {
    this.gerritService
      .getChangesOfProject(projectName)
      .subscribe((data: ChangeInfo[]) => {
        this.commitList = data;
      });
  }

  requestReview(entry: ReviewEntry) {
    console.log('Request review for', entry);
  }

  navigateToChangeDetails(changeId: string) {
    this.router.navigate(['/change-detail', changeId]);
  }
}
