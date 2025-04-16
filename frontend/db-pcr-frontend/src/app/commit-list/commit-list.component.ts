import { NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ChangeInfo } from '../interface/gerrit/change-info';
import { ActivatedRoute, Router } from '@angular/router';
import { GerritService } from '../http/gerrit.service';

@Component({
  selector: 'app-commit-list',
  imports: [NgIf, NgFor],
  templateUrl: './commit-list.component.html',
  styleUrl: './commit-list.component.css',
})
export class CommitListComponent implements OnInit {
  projectName: string = '';
  commitList: ChangeInfo[] = [];

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

  navigateToChangeDetails(changeId: string) {
    this.router.navigate(['/change-detail', changeId]);
  }
}
