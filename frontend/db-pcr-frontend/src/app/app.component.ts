import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NgFor } from '@angular/common';
import { CommitInfo } from './interface/commit-info';
import { GerritService } from './gerrit.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NgFor],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  title = 'db-cpr-frontend';

  commitList: CommitInfo[] = [];
  anonymousCommitList: CommitInfo[] = [];

  constructor(private gerritService: GerritService) {}

  ngOnInit() {
    this.gerritService.getCommitList().subscribe((data: CommitInfo[]) => {
      this.commitList = data;
    });

    this.gerritService
      .getAnonymousCommitList()
      .subscribe((data: CommitInfo[]) => {
        this.anonymousCommitList = data;
      });
  }
}
