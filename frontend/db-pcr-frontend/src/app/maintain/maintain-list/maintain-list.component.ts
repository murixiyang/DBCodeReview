import { Component } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { ProjectDto } from '../../interface/database/project-dto';
import { ProjectService } from '../../http/project.service';

@Component({
  selector: 'app-maintain-list',
  imports: [NgFor, AsyncPipe],
  templateUrl: './maintain-list.component.html',
  styleUrl: './maintain-list.component.css',
})
export class MaintainListComponent {
  projects$!: Observable<ProjectDto[]>;

  constructor(private projectSvc: ProjectService, private router: Router) {}

  ngOnInit() {
    this.projects$ = this.projectSvc.getGroupProjects();
  }

  navigateToCommitList(projectId: number) {
    this.router.navigate(['/maintain', projectId]);
  }
}
