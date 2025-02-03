import { Component, OnInit } from '@angular/core';
import { ProjectInfoModel } from '../interface/project-info';
import { ChangeInfo } from '../interface/change-info';
import { ModiFileInfo } from '../interface/modi-file-info';
import { GerritService } from '../http/gerrit.service';
import { Router } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-project-list',
  imports: [NgIf, NgFor],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.css',
})
export class ProjectListComponent implements OnInit {
  projectList: ProjectInfoModel[] = [];
  selectedProject: string = '';

  changeList: ChangeInfo[] = [];

  modiFileList: ModiFileInfo[] = [];

  constructor(private gerritService: GerritService, private router: Router) {}

  ngOnInit() {
    this.gerritService
      .getProjectList()
      .subscribe((data: ProjectInfoModel[]) => {
        this.projectList = data;
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

  getModiFileList(changeId: string, revisionId: string) {
    this.gerritService
      .getModifiedFileInChange(changeId, revisionId)
      .subscribe((data: ModiFileInfo[]) => {
        this.modiFileList = data;
      });
  }

  navigateToChangeDetails(changeId: string) {
    console.log('changeId', changeId);
    this.router.navigate(['/change-detail', changeId]);
  }
}
