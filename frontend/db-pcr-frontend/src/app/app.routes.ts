import { Routes } from '@angular/router';
import { ProjectListComponent } from './project-list/project-list.component';

import { MaintainListComponent } from './maintain/maintain-list/maintain-list.component';
import { CommitListComponent } from './author-commit/commit-list/commit-list.component';
import { CommitDetailComponent } from './author-commit/commit-details/commit-details.component';
import { ReviewListComponent } from './review/review-list/review-list.component';
import { ReviewDetailComponent } from './review/review-detail/review-detail.component';
import { AuthorReviewDetailComponent } from './author-commit/author-review-detail/author-review-detail.component';
import { EvalListComponent } from './evaluation/eval-list/eval-list.component';
import { EvalIntroComponent } from './evaluation/eval-intro/eval-intro.component';
import { EvalAuthorComponent } from './evaluation/eval-author/eval-author.component';
import { EvalReviewComponent } from './evaluation/eval-review/eval-review.component';
import { EvalSurveyComponent } from './evaluation/eval-survey/eval-survey.component';

export const routes: Routes = [
  { path: 'project-list', component: ProjectListComponent },
  { path: 'commit-list/:projectId', component: CommitListComponent },
  {
    path: 'author/detail/:gerritChangeId/:assignmentId',
    component: AuthorReviewDetailComponent,
  },
  { path: 'commit-detail/:projectId/:sha', component: CommitDetailComponent },
  { path: 'review/:projectId', component: ReviewListComponent },
  {
    path: 'review/detail/:gerritChangeId/:assignmentId',
    component: ReviewDetailComponent,
  },
  { path: 'maintain/project-list', component: MaintainListComponent },
  { path: 'eval/intro', component: EvalIntroComponent },
  { path: 'eval/author', component: EvalAuthorComponent },
  { path: 'eval/review/:round', component: EvalReviewComponent },
  { path: 'eval/survey', component: EvalSurveyComponent },
  { path: '', redirectTo: '/eval/intro', pathMatch: 'full' },
];
