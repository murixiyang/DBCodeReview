import { Routes } from '@angular/router';
import { ChangeDetailComponent } from './change-detail/change-detail.component';

export const routes: Routes = [
  { path: 'change-detail/:id', component: ChangeDetailComponent },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
];
