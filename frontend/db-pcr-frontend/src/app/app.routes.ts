import { Routes } from '@angular/router';
import { CommitDetailComponent } from './commit-details/commit-details.component';
import { ProjectListComponent } from './project-list/project-list.component';

import { CommitListComponent } from './commit-list/commit-list.component';
import { MaintainListComponent } from './maintain/maintain-list/maintain-list.component';

export const routes: Routes = [
  { path: 'project-list', component: ProjectListComponent },
  { path: 'commit-list/:projectId', component: CommitListComponent },
  { path: 'commit-detail/:projectId/:sha', component: CommitDetailComponent },
  { path: 'maintain/project-list', component: MaintainListComponent },
  { path: '', redirectTo: '/project-list', pathMatch: 'full' },
];
