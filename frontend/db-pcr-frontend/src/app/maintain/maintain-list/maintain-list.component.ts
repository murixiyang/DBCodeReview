import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { GitlabService } from '../../http/gitlab.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-maintain-list',
  imports: [],
  templateUrl: './maintain-list.component.html',
  styleUrl: './maintain-list.component.css',
})
export class MaintainListComponent {
  projects$!: Observable<String[]>;

  constructor(private gitLabService: GitlabService, private router: Router) {}

  ngOnInit() {
    this.projects$ = this.gitLabService.getGroupProjects();
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/commit-list', projectId]);
  }
}
