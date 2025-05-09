import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { GitlabService } from '../../http/gitlab.service';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { ProjectSchema } from '@gitbeaker/rest';

@Component({
  selector: 'app-maintain-list',
  imports: [NgFor, AsyncPipe],
  templateUrl: './maintain-list.component.html',
  styleUrl: './maintain-list.component.css',
})
export class MaintainListComponent {
  projects$!: Observable<ProjectSchema[]>;

  constructor(private gitLabService: GitlabService, private router: Router) {}

  ngOnInit() {
    this.projects$ = this.gitLabService.getGroupProjects();
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/maintain', projectId]);
  }
}
