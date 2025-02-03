import { Routes } from '@angular/router';
import { ChangeDetailsComponent } from './change-details/change-details.component';
import { ProjectListComponent } from './project-list/project-list.component';

export const routes: Routes = [
  { path: 'project-list', component: ProjectListComponent },
  { path: 'change-detail/:id', component: ChangeDetailsComponent },
  { path: '', redirectTo: '/project-list', pathMatch: 'full' },
];
