import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { AsyncPipe, NgFor } from '@angular/common';
import { ProjectDto } from '../../interface/database/project-dto';
import { ProjectService } from '../../http/project.service';

@Component({
  selector: 'app-maintain-list',
  imports: [NgFor],
  templateUrl: './maintain-list.component.html',
  styleUrl: './maintain-list.component.css',
})
export class MaintainListComponent implements OnInit {
  groupProjects!: ProjectDto[];

  constructor(private projectSvc: ProjectService, private router: Router) {}

  ngOnInit() {
    this.projectSvc.getGroupProjects().subscribe((data) => {
      this.groupProjects = data;
    });
  }

  navigateToCommitList(groupProjectId: number) {
    this.router.navigate(['/maintain', groupProjectId]);
  }
}
