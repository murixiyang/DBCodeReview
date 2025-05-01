import { Routes } from '@angular/router';
import { ChangeDetailsComponent } from './change-details/change-details.component';
import { ProjectListComponent } from './project-list/project-list.component';

import { CommitListComponent } from './commit-list/commit-list.component';

export const routes: Routes = [
  { path: 'project-list', component: ProjectListComponent },
  { path: 'commit-list/:project-id', component: CommitListComponent },
  { path: 'change-detail/:change-id', component: ChangeDetailsComponent },
  { path: '', redirectTo: '/project-list', pathMatch: 'full' },
];
