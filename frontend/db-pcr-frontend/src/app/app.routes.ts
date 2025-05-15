import { Routes } from '@angular/router';
import { ProjectListComponent } from './project-list/project-list.component';

import { MaintainListComponent } from './maintain/maintain-list/maintain-list.component';
import { MaintainDetailComponent } from './maintain/maintain-detail/maintain-detail.component';
import { CommitListComponent } from './author-commit/commit-list/commit-list.component';
import { CommitDetailComponent } from './author-commit/commit-details/commit-details.component';
import { ReviewListComponent } from './review/review-list/review-list.component';
import { ReviewDetailComponent } from './review/review-detail/review-detail.component';

export const routes: Routes = [
  { path: 'project-list', component: ProjectListComponent },
  { path: 'commit-list/:projectId', component: CommitListComponent },
  { path: 'commit-detail/:projectId/:sha', component: CommitDetailComponent },
  { path: 'review/:projectId', component: ReviewListComponent },
  {
    path: 'review/:gerritChangeId',
    component: ReviewDetailComponent,
  },
  { path: 'maintain/project-list', component: MaintainListComponent },
  { path: 'maintain/:projectId', component: MaintainDetailComponent },
  { path: '', redirectTo: '/project-list', pathMatch: 'full' },
];
