import { Component, OnInit } from '@angular/core';
import { ProjectInfoModel } from '../interface/project-info';
import { ChangeInfo } from '../interface/change-info';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import { Router } from '@angular/router';
import { KeyValuePipe, NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-project-list',
  imports: [NgIf, NgFor, KeyValuePipe],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projectMap: Map<string, ProjectInfoModel> = new Map<
    string,
    ProjectInfoModel
  >();
  selectedProject: string = '';

  changeList: ChangeInfo[] = [];

  constructor(private gerritService: GerritService, private router: Router) {}

  ngOnInit() {
    this.gerritService
      .getProjectList()
      .subscribe((dataMap: Map<string, ProjectInfoModel>) => {
        this.projectMap = dataMap;
      });
  }

  onSelectProject(projectName: string) {
    this.selectedProject = projectName;
    this.getChangesOfProject(projectName);
  }

  getChangesOfProject(projectName: string) {
    this.gerritService
      .getChangesOfProject(projectName)
      .subscribe((data: ChangeInfo[]) => {
        this.changeList = data;
      });
  }

  navigateToChangeDetails(changeId: string) {
    this.router.navigate(['/change-detail', changeId]);
  }
}
