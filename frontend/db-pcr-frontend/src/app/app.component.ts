import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NgFor, NgIf } from '@angular/common';
import { ChangeInfo } from './interface/change-info';
import { GerritService } from './http/gerrit.service';
import { ProjectInfoModel } from './interface/project-info';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NgFor, NgIf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit {
  title = 'db-cpr-frontend';

  projectList: ProjectInfoModel[] = [];
  selectedProject: string = '';

  changeList: ChangeInfo[] = [];
  anonymousCommitList: ChangeInfo[] = [];

  constructor(private gerritService: GerritService) {}

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
        console.log('data', data);
        this.changeList = data;
      });
  }
}
